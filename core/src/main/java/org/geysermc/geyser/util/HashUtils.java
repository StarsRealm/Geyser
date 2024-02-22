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

package org.geysermc.geyser.util;

import org.cloudburstmc.nbt.NBTOutputStream;
import org.cloudburstmc.nbt.NbtMap;
import org.cloudburstmc.nbt.NbtMapBuilder;
import org.cloudburstmc.nbt.NbtUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.TreeMap;

public class HashUtils {
    private static final int FNV1_32_INIT = 0x811c9dc5;
    private static final int FNV1_PRIME_32 = 0x01000193;

    public static int createHash(NbtMap block) {
        if ("minecraft:unknown".equals(block.getString("name"))) {
            return -2; // This is a special case
        }

        NbtMapBuilder tagBuilder = NbtMap.builder()
                .putString("name", block.getString("name"))
                .putCompound("states", NbtMap.fromMap(new TreeMap<>(block.getCompound("states"))));
        NbtMap tag = tagBuilder.build();

        byte[] bytes;
        try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
            try (NBTOutputStream writer = NbtUtils.createWriterLE(stream)) {
                writer.writeTag(tag);
                bytes = stream.toByteArray();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return fnv1a_32(bytes);
    }

    private static int fnv1a_32(byte[] data) {
        int hash = FNV1_32_INIT;
        for (byte datum : data) {
            hash ^= (datum & 0xff);
            hash *= FNV1_PRIME_32;
        }
        return hash;
    }
}
