/*
 * MIT License
 *
 * Copyright (c) 2019 Glare
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package me.glaremasters.guilds.cooldowns;

import com.google.common.base.Preconditions;

import java.util.Arrays;
import java.util.UUID;

/**
 * Created by Glare
 * Date: 5/14/2019
 * Time: 9:27 PM
 */
public class Cooldown {
    private final UUID cooldownId;
    private final Type cooldownType;
    private final UUID cooldownOwner;
    private final Long cooldownExpiry;

    public Cooldown(String id, String cooldownType, String cooldownOwner, Long cooldownExpiry) {
        this(UUID.fromString(id), Type.getByTypeName(cooldownType), UUID.fromString(cooldownOwner), cooldownExpiry);
    }

    public Cooldown(String cooldownType, String cooldownOwner, Long cooldownExpiry) {
        this(Type.getByTypeName(cooldownType), UUID.fromString(cooldownOwner), cooldownExpiry);
    }

    public Cooldown(Type cooldownType, UUID cooldownOwner, Long cooldownExpiry) {
        this(UUID.randomUUID(), cooldownType, cooldownOwner, cooldownExpiry);
    }

    public Cooldown(UUID cooldownId, Type cooldownType, UUID cooldownOwner, Long cooldownExpiry) {
        Preconditions.checkNotNull(cooldownType, "cooldown type");
        Preconditions.checkNotNull(cooldownOwner, "cooldown owner");
        this.cooldownId = cooldownId;
        this.cooldownType = cooldownType;
        this.cooldownOwner = cooldownOwner;
        this.cooldownExpiry = cooldownExpiry;
    }

    public UUID getCooldownId() {
        return cooldownId;
    }

    public Type getCooldownType() {
        return cooldownType;
    }

    public UUID getCooldownOwner() {
        return cooldownOwner;
    }

    public Long getCooldownExpiry() {
        return cooldownExpiry;
    }

    public enum Type {
        Request("request"),
        SetHome("sethome"),
        Home("home"),
        Buffs("buffs"),
        Join("join");

        private final String typeName;

        Type(String typeName) {
            this.typeName = typeName;
        }

        public String getTypeName() {
            return typeName;
        }

        public static Type getByTypeName(String typeName) {
            return Arrays.stream(values()).filter(c -> c.getTypeName().equalsIgnoreCase(typeName)).findFirst().orElse(null);
        }
    }
}
