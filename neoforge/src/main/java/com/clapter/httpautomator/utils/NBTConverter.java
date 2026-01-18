package com.clapter.httpautomator.utils;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class NBTConverter {

    public static <K, V> CompoundTag convertMapToNBT(Map<K, V> map, Function<K, String> keySerializer, Function<V, String> valueSerializer) {
        CompoundTag compound = new CompoundTag();
        for (Map.Entry<K, V> entry : map.entrySet()) {
            compound.putString(keySerializer.apply(entry.getKey()), valueSerializer.apply(entry.getValue()));
        }
        return compound;
    }

    public static <K, V> HashMap<K, V> convertNBTToMap(CompoundTag compound, Function<String, K> keyDeserializer, Function<String, V> valueDeserializer) {
        HashMap<K, V> map = new HashMap<>();
        for (String key : compound.getAllKeys()) {
            Tag tag = compound.get(key);
            if (tag instanceof StringTag) {
                map.put(keyDeserializer.apply(key), valueDeserializer.apply(((StringTag) tag).getAsString()));
            }
        }
        return map;
    }
}
