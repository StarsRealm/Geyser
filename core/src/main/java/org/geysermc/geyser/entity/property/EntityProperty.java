/*
 * Copyright (c) 2019-2024 GeyserMC. http://geysermc.org
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

package org.geysermc.geyser.entity.property;

import lombok.Getter;
import org.cloudburstmc.nbt.NbtList;
import org.cloudburstmc.nbt.NbtMap;
import org.cloudburstmc.nbt.NbtMapBuilder;
import org.cloudburstmc.nbt.NbtType;
import org.cloudburstmc.protocol.bedrock.packet.SyncEntityPropertyPacket;
import org.geysermc.geyser.GeyserImpl;
import org.geysermc.geyser.event.type.GeyserDefineEntityPropertyEventImpl;

import java.util.*;

public abstract class EntityProperty {

    private static final String PLAYER_KEY = "minecraft:player";
    private static final String PROPERTIES_KEY = "properties";

    private static final Map<String, List<EntityProperty>> entityPropertyMap = new HashMap<>();
    @Getter
    private static List<SyncEntityPropertyPacket> packetCache = new ArrayList<>();
    @Getter
    private static NbtMap playerPropertyCache = NbtMap.EMPTY;

    @Getter
    private final String identifier;

    public EntityProperty(String identifier) {
        this.identifier = identifier;
    }

    public static boolean register(String entityIdentifier, EntityProperty property) {
        List<EntityProperty> entityProperties = entityPropertyMap.getOrDefault(entityIdentifier, new ArrayList<>());
        for (EntityProperty entityProperty : entityProperties) {
            if (Objects.equals(entityProperty.identifier, property.identifier)) return false;
        }
        entityProperties.add(property);
        entityPropertyMap.put(entityIdentifier, entityProperties);
        return true;
    }

    public static void buildPacket() {

        GeyserImpl.getInstance().eventBus().fire(new GeyserDefineEntityPropertyEventImpl());

        List<SyncEntityPropertyPacket> packetList = new ArrayList<>();
        for (Map.Entry<String, List<EntityProperty>> entry : entityPropertyMap.entrySet()) {
            NbtList<NbtMap> listProperty = buildPropertyList(entry.getValue());
            NbtMap nbt = NbtMap.builder().putList(PROPERTIES_KEY, NbtType.COMPOUND,listProperty).putString("type", entry.getKey()).build();
            SyncEntityPropertyPacket packet = new SyncEntityPropertyPacket();
            packet.setData(nbt);
            packetList.add(packet);
        }
        packetCache = packetList;
    }

    public static void buildPlayerProperty() {
        List<EntityProperty> properties = entityPropertyMap.get(PLAYER_KEY);
        if (properties == null) {
            playerPropertyCache = NbtMap.EMPTY;
            return;
        }
        NbtList<NbtMap> listProperty = buildPropertyList(properties);
        playerPropertyCache = NbtMap.builder().putList(PROPERTIES_KEY, NbtType.COMPOUND, listProperty).putString("type", PLAYER_KEY).build();
    }

    public static List<EntityProperty> getEntityProperty(String identifier) {
        return entityPropertyMap.getOrDefault(identifier, new ArrayList<>());
    }

    public abstract void populateTag(NbtMapBuilder tag);

    private static NbtList<NbtMap> buildPropertyList(List<EntityProperty> properties) {
        NbtList<NbtMap> listProperty = new NbtList<>(NbtType.COMPOUND);
        for (EntityProperty entityProperty : properties) {
            NbtMapBuilder propertyTag = NbtMap.builder().putString("name", entityProperty.getIdentifier());
            entityProperty.populateTag(propertyTag);
            listProperty.add(propertyTag.build());
        }
        return listProperty;
    }
}
