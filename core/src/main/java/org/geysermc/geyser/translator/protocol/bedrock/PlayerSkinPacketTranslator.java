/*
 * Copyright (c) 2024 GeyserMC. http://geysermc.org
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 * @author GeyserMC
 * @link https://github.com/GeyserMC/Geyser
 */

package org.geysermc.geyser.translator.protocol.bedrock;

import org.cloudburstmc.protocol.bedrock.data.skin.SerializedSkin;
import org.cloudburstmc.protocol.bedrock.packet.PlayerSkinPacket;
import org.geysermc.geyser.GeyserImpl;
import org.geysermc.geyser.GeyserLogger;
import org.geysermc.geyser.entity.type.player.SessionPlayerEntity;
import org.geysermc.geyser.session.GeyserSession;
import org.geysermc.geyser.translator.protocol.PacketTranslator;
import org.geysermc.geyser.translator.protocol.Translator;

import java.util.concurrent.TimeUnit;

@Translator(packet = PlayerSkinPacket.class)
public class PlayerSkinPacketTranslator extends PacketTranslator<PlayerSkinPacket> {
    private static final int skinChangeCooldown = 1;

    @Override
    public void translate(GeyserSession session, PlayerSkinPacket packet) {
        GeyserLogger logger = GeyserImpl.getInstance().getLogger();
        SerializedSkin skin = packet.getSkin();

        SessionPlayerEntity playerEntity = session.getPlayerEntity();

        if (!skin.isValid()) {
            logger.error(playerEntity.getUsername() + ": PlayerSkinPacket with invalid skin");
            return;
        }

        var tooQuick = System.currentTimeMillis() - session.getLastSkinChange() < TimeUnit.SECONDS.toMillis(skinChangeCooldown);
        if (tooQuick) {
            logger.warning("Player " + playerEntity.getUsername() + " change skin too quick!");
            return;
        }
        SerializedSkin.Builder builder = SerializedSkin.builder()
                .skinData(skin.getSkinData())
                .capeData(skin.getCapeData())
                .geometryName(skin.getGeometryName())
                .fullSkinId(skin.getFullSkinId())
                .skinColor(skin.getSkinColor())
                .playFabId(skin.getPlayFabId())
                .geometryData(skin.getGeometryData())
                .skinResourcePatch(skin.getSkinResourcePatch())
                .animations(skin.getAnimations())
                .animationData(skin.getAnimationData())
                .armSize(skin.getArmSize())
                .premium(skin.isPremium())
                .capeId(skin.getCapeId())
                .capeOnClassic(skin.isCapeOnClassic())
                .personaPieces(skin.getPersonaPieces())
                .tintColors(skin.getTintColors())
                .persona(skin.isPersona())
                .geometryDataEngineVersion(skin.getGeometryDataEngineVersion())
                .primaryUser(skin.isPrimaryUser());
        String fullSkinId = skin.getFullSkinId();
        if (skin.getCapeId() != null) {
            builder.skinId(fullSkinId.substring(0, fullSkinId.length() - skin.getCapeId().length()));
        } else {
            builder.skinId(fullSkinId);
        }
        SerializedSkin serializedSkin = builder.build();

        session.setLastSkinChange(System.currentTimeMillis());
        session.setBedrockSkin(serializedSkin);

        PlayerSkinPacket pk = new PlayerSkinPacket();
        pk.setUuid(session.getAuthData().uuid());
        pk.setOldSkinName("");
        pk.setNewSkinName(serializedSkin.getSkinId());
        pk.setSkin(serializedSkin);
        pk.setTrustedSkin(true);
        GeyserImpl.getInstance().broadcastUpstreamPacket(pk);
    }

}
