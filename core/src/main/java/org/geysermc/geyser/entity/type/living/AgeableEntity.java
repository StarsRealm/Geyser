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

package org.geysermc.geyser.entity.type.living;

import com.github.steveice10.mc.protocol.data.game.entity.metadata.type.BooleanEntityMetadata;
import org.cloudburstmc.math.vector.Vector3f;
import org.cloudburstmc.protocol.bedrock.data.entity.EntityDataTypes;
import org.cloudburstmc.protocol.bedrock.data.entity.EntityFlag;
import org.geysermc.geyser.entity.GeyserEntityDefinition;
import org.geysermc.geyser.session.GeyserSession;
import java.util.Map;
import java.util.UUID;

public class AgeableEntity extends CreatureEntity {

    public AgeableEntity(GeyserSession session, int entityId, long geyserId, UUID uuid, GeyserEntityDefinition<?> definition, Vector3f position, Vector3f motion, float yaw, float pitch, float headYaw, Map<String, Integer> intEntityProperty, Map<String, Float> floatEntityProperty){
        super(session, entityId, geyserId, uuid, definition, position, motion, yaw, pitch, headYaw, intEntityProperty, floatEntityProperty);
    }

    @Override
    protected void initializeMetadata() {
        super.initializeMetadata();
        // Required as of 1.19.3 Java
        dirtyMetadata.put(EntityDataTypes.SCALE, getAdultSize());
    }

    public void setBaby(BooleanEntityMetadata entityMetadata) {
        boolean isBaby = entityMetadata.getPrimitiveValue();
        dirtyMetadata.put(EntityDataTypes.SCALE, isBaby ? getBabySize() : getAdultSize());
        setFlag(EntityFlag.BABY, isBaby);

        setBoundingBoxHeight(definition.height() * (isBaby ? getBabySize() : getAdultSize()));
        setBoundingBoxWidth(definition.width() * (isBaby ? getBabySize() : getAdultSize()));
    }

    /**
     * The scale that should be used when this entity is not a baby.
     */
    protected float getAdultSize() {
        return 1f;
    }

    /**
     * The scale that should be used when this entity is a baby.
     */
    protected float getBabySize() {
        return 0.55f;
    }

    public boolean isBaby() {
        return getFlag(EntityFlag.BABY);
    }
}
