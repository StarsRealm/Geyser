/*
 * Copyright (c) 2019-2022 GeyserMC. http://geysermc.org
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

package org.geysermc.geyser.translator.protocol.java;


import com.google.common.base.Charsets;
import org.cloudburstmc.math.vector.Vector3f;
import org.cloudburstmc.protocol.bedrock.data.entity.EntityEventType;
import org.cloudburstmc.protocol.bedrock.data.inventory.ItemData;
import org.cloudburstmc.protocol.bedrock.data.inventory.ItemUseType;
import org.cloudburstmc.protocol.bedrock.data.inventory.transaction.InventoryActionData;
import org.cloudburstmc.protocol.bedrock.data.inventory.transaction.InventorySource;
import org.cloudburstmc.protocol.bedrock.data.inventory.transaction.InventoryTransactionType;
import org.cloudburstmc.protocol.bedrock.packet.AnimateEntityPacket;
import org.cloudburstmc.protocol.bedrock.packet.CompletedUsingItemPacket;
import org.cloudburstmc.protocol.bedrock.packet.EntityEventPacket;
import org.cloudburstmc.protocol.bedrock.packet.InventoryTransactionPacket;
import org.cloudburstmc.protocol.bedrock.packet.SpawnParticleEffectPacket;
import org.cloudburstmc.protocol.bedrock.packet.TransferPacket;
import org.cloudburstmc.protocol.bedrock.packet.UnknownPacket;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.geysermc.cumulus.Forms;
import org.geysermc.cumulus.form.Form;
import org.geysermc.cumulus.form.util.FormType;
import org.geysermc.erosion.Constants;
import org.geysermc.erosion.packet.ErosionPacket;
import org.geysermc.erosion.packet.Packets;
import org.geysermc.erosion.packet.geyserbound.GeyserboundPacket;
import org.geysermc.floodgate.pluginmessage.PluginMessageChannels;
import org.geysermc.geyser.GeyserImpl;
import org.geysermc.geyser.GeyserLogger;
import org.geysermc.geyser.entity.properties.GeyserEntityPropertyManager;
import org.geysermc.geyser.item.Items;
import org.geysermc.geyser.session.GeyserSession;
import org.geysermc.geyser.translator.item.ItemTranslator;
import org.geysermc.geyser.translator.protocol.PacketTranslator;
import org.geysermc.geyser.translator.protocol.Translator;
import org.geysermc.geyser.util.DimensionUtils;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftCodecHelper;
import org.geysermc.mcprotocollib.protocol.data.game.item.ItemStack;
import org.geysermc.mcprotocollib.protocol.packet.common.clientbound.ClientboundCustomPayloadPacket;
import org.geysermc.mcprotocollib.protocol.packet.common.serverbound.ServerboundCustomPayloadPacket;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

@Translator(packet = ClientboundCustomPayloadPacket.class)
public class JavaCustomPayloadTranslator extends PacketTranslator<ClientboundCustomPayloadPacket> {
    private final GeyserLogger logger = GeyserImpl.getInstance().getLogger();

    @Override
    public void translate(GeyserSession session, ClientboundCustomPayloadPacket packet) {
        String channel = packet.getChannel().asString();

        switch (channel) {
            case PluginMessageChannels.CROSS_BOW_COMPLETED -> {
                long id = session.getPlayerEntity().getGeyserId();
                EntityEventPacket entityEventPacket = new EntityEventPacket();
                entityEventPacket.setRuntimeEntityId(id);
                entityEventPacket.setType(EntityEventType.FINISHED_CHARGING_ITEM);
                entityEventPacket.setData(0);

                CompletedUsingItemPacket completedUsingItemPacket = new CompletedUsingItemPacket();
                completedUsingItemPacket.setItemId(Items.CROSSBOW.toBedrockDefinition(null, session.getItemMappings()).getBedrockDefinition().getRuntimeId());
                completedUsingItemPacket.setType(ItemUseType.UNKNOWN);

                MinecraftCodecHelper helper = session.getProtocol().createHelper();
                ByteBuf buffer = Unpooled.wrappedBuffer(packet.getData());
                ItemStack from = helper.readOptionalItemStack(buffer);
                ItemStack to = helper.readOptionalItemStack(buffer);
                InventoryTransactionPacket inventoryTransactionPacket = new InventoryTransactionPacket();
                ItemData itemInHand = ItemTranslator.translateToBedrock(session, from);
                inventoryTransactionPacket.getActions().add(new InventoryActionData(InventorySource.fromContainerWindowId(0), session.getPlayerInventory().getHeldItemSlot(),
                    itemInHand, ItemTranslator.translateToBedrock(session, to)));
                inventoryTransactionPacket.setTransactionType(InventoryTransactionType.ITEM_RELEASE);
                inventoryTransactionPacket.setActionType(1);
                inventoryTransactionPacket.setRuntimeEntityId(id);
                inventoryTransactionPacket.setHotbarSlot(session.getPlayerInventory().getHeldItemSlot());
                inventoryTransactionPacket.setItemInHand(itemInHand);

                session.sendUpstreamPacket(entityEventPacket);
                session.sendUpstreamPacket(completedUsingItemPacket);
                session.sendUpstreamPacket(inventoryTransactionPacket);
            }
            case PluginMessageChannels.ENTITY_PROPERTY -> {
                MinecraftCodecHelper helper = session.getProtocol().createHelper();
                ByteBuf buffer = Unpooled.wrappedBuffer(packet.getData());
                int entityId = helper.readVarInt(buffer);
                try {
                    GeyserEntityPropertyManager propertyManager = session.getEntityCache().getEntityByJavaId(entityId).getPropertyManager();
                    Map<String, Integer> integerMap = helper.readStringIntMap(buffer);
                    Map<String, Float> floatMap = helper.readStringFloatMap(buffer);
                    integerMap.forEach(propertyManager::add);
                    floatMap.forEach(propertyManager::add);
                    int size = helper.readVarInt(buffer);
                    for (int i = 0; i < size; i++) {
                        String name = helper.readString(buffer);
                        boolean value = buffer.readBoolean();
                        propertyManager.add(name, value);
                    }
                    size = helper.readVarInt(buffer);
                    for (int i = 0; i < size; i++) {
                        String name = helper.readString(buffer);
                        String value = helper.readString(buffer);
                        propertyManager.add(name, value);
                    }
                } catch (Exception e) {
                    GeyserImpl.getInstance().getLogger().warning("Failed to parse entity property: " + entityId);
                }
            }
            case PluginMessageChannels.BOOLEAN_PROPERTY -> {
                MinecraftCodecHelper helper = session.getProtocol().createHelper();
                ByteBuf buffer = Unpooled.wrappedBuffer(packet.getData());
                int entityId = helper.readVarInt(buffer);
                String name = helper.readString(buffer);
                boolean value = buffer.readBoolean();
                session.getEntityCache().getEntityByJavaId(entityId).getPropertyManager().add(name, value);
            }
            case PluginMessageChannels.INTEGER_PROPERTY -> {
                MinecraftCodecHelper helper = session.getProtocol().createHelper();
                ByteBuf buffer = Unpooled.wrappedBuffer(packet.getData());
                int entityId = helper.readVarInt(buffer);
                String name = helper.readString(buffer);
                int value = helper.readVarInt(buffer);
                session.getEntityCache().getEntityByJavaId(entityId).getPropertyManager().add(name, value);
            }
            case PluginMessageChannels.FLOAT_PROPERTY -> {
                MinecraftCodecHelper helper = session.getProtocol().createHelper();
                ByteBuf buffer = Unpooled.wrappedBuffer(packet.getData());
                int entityId = helper.readVarInt(buffer);
                String name = helper.readString(buffer);
                float value = buffer.readFloat();
                session.getEntityCache().getEntityByJavaId(entityId).getPropertyManager().add(name, value);
            }
            case PluginMessageChannels.STRING_PROPERTY -> {
                MinecraftCodecHelper helper = session.getProtocol().createHelper();
                ByteBuf buffer = Unpooled.wrappedBuffer(packet.getData());
                int entityId = helper.readVarInt(buffer);
                String name = helper.readString(buffer);
                String value = helper.readString(buffer);
                session.getEntityCache().getEntityByJavaId(entityId).getPropertyManager().add(name, value);
            }
            case PluginMessageChannels.BEDROCK_PARTICLE -> {
                MinecraftCodecHelper helper = session.getProtocol().createHelper();
                ByteBuf buffer = Unpooled.wrappedBuffer(packet.getData());

                SpawnParticleEffectPacket spawnParticleEffectPacket = new SpawnParticleEffectPacket();
                spawnParticleEffectPacket.setIdentifier(helper.readString(buffer));
                spawnParticleEffectPacket.setPosition(Vector3f.from(buffer.readDouble(), buffer.readDouble(), buffer.readDouble()));
                spawnParticleEffectPacket.setUniqueEntityId(session.getEntityCache().getEntityByJavaId(helper.readVarInt(buffer)).getGeyserId());
                spawnParticleEffectPacket.setDimensionId(DimensionUtils.javaToBedrock(session));

                if (buffer.readBoolean()) {
                    spawnParticleEffectPacket.setMolangVariablesJson(Optional.of(helper.readString(buffer)));
                }

                session.sendUpstreamPacket(spawnParticleEffectPacket);
            }
            case PluginMessageChannels.ENTITY_ANIMATION -> {
                MinecraftCodecHelper helper = session.getProtocol().createHelper();
                ByteBuf buffer = Unpooled.wrappedBuffer(packet.getData());

                AnimateEntityPacket animateEntityPacket = new AnimateEntityPacket();
                animateEntityPacket.setAnimation(helper.readString(buffer));
                animateEntityPacket.setNextState(helper.readString(buffer));
                animateEntityPacket.setStopExpression(helper.readString(buffer));
                animateEntityPacket.setStopExpressionVersion(helper.readVarInt(buffer));
                animateEntityPacket.setController(helper.readString(buffer));
                animateEntityPacket.setBlendOutTime(buffer.readFloat());
                animateEntityPacket.getRuntimeEntityIds().add(session.getEntityCache().getEntityByJavaId(helper.readVarInt(buffer)).getGeyserId());

                session.sendUpstreamPacket(animateEntityPacket);
            }
            case Constants.PLUGIN_MESSAGE -> {
                ByteBuf buf = Unpooled.wrappedBuffer(packet.getData());
                ErosionPacket<?> erosionPacket = Packets.decode(buf);
                ((GeyserboundPacket) erosionPacket).handle(session.getErosionHandler());
                return;
            }
            case PluginMessageChannels.FORM -> session.ensureInEventLoop(() -> {
                byte[] data = packet.getData();

                // receive: first byte is form type, second and third are the id, remaining is the form data
                // respond: first and second byte id, remaining is form response data

                FormType type = FormType.fromOrdinal(data[0]);
                if (type == null) {
                    throw new NullPointerException("Got type " + data[0] + " which isn't a valid form type!");
                }

                String dataString = new String(data, 3, data.length - 3, Charsets.UTF_8);

                Form form = Forms.fromJson(dataString, type, (ignored, response) -> {
                    byte[] finalData;
                    if (response == null) {
                        // Response data can be null as of 1.19.20 (same behaviour as empty response data)
                        // Only need to send the form id
                        finalData = new byte[]{data[1], data[2]};
                    } else {
                        byte[] raw = response.getBytes(StandardCharsets.UTF_8);
                        finalData = new byte[raw.length + 2];

                        finalData[0] = data[1];
                        finalData[1] = data[2];
                        System.arraycopy(raw, 0, finalData, 2, raw.length);
                    }

                    session.sendDownstreamPacket(new ServerboundCustomPayloadPacket(packet.getChannel(), finalData));
                });
                session.sendForm(form);
            });
            case PluginMessageChannels.TRANSFER -> session.ensureInEventLoop(() -> {
                byte[] data = packet.getData();

                // port (4 bytes), address (remaining data)
                if (data.length < 5) {
                    throw new NullPointerException("Transfer data should be at least 5 bytes long");
                }

                int port = data[0] << 24 | (data[1] & 0xFF) << 16 | (data[2] & 0xFF) << 8 | data[3] & 0xFF;
                String address = new String(data, 4, data.length - 4);

                if (logger.isDebug()) {
                    logger.info("Transferring client to: " + address + ":" + port);
                }

                TransferPacket transferPacket = new TransferPacket();
                transferPacket.setAddress(address);
                transferPacket.setPort(port);
                session.sendUpstreamPacket(transferPacket);
            });
            case PluginMessageChannels.PACKET -> session.ensureInEventLoop(() -> {
                logger.debug("A packet has been sent using the Floodgate api");
                byte[] data = packet.getData();

                // packet id, packet data
                if (data.length < 2) {
                    throw new IllegalStateException("Packet data should be at least 2 bytes long");
                }

                int packetId = data[0] & 0xFF;
                ByteBuf packetData = Unpooled.wrappedBuffer(data, 1, data.length - 1);

                var toSend = new UnknownPacket();
                toSend.setPacketId(packetId);
                toSend.setPayload(packetData);

                session.sendUpstreamPacket(toSend);
            });
        }
    }

    @Override
    public boolean shouldExecuteInEventLoop() {
        // For Erosion packets
        return false;
    }
}
