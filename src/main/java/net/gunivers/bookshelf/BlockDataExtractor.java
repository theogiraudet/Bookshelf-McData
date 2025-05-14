package net.gunivers.bookshelf;

import static net.gunivers.bookshelf.Extractor.writeJsonToFile;
import com.google.gson.JsonObject;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;

public class BlockDataExtractor {

    public static void generateBlockSounds(String path, String fileName) throws IOException {
        System.out.println("Generating block data...");
        Files.createDirectories(Path.of(path));
        JsonObject blockSounds = extractBlocksData();
        writeJsonToFile(path + fileName + ".json", blockSounds, true);
        writeJsonToFile(path + fileName + ".min.json", blockSounds, false);
    }

    private static JsonObject extractBlocksData() {
        JsonObject blocksJson = new JsonObject();
        for (Field blockField : Blocks.class.getFields()) {
            try {
                Block block = (Block) blockField.get(null);
                String blockID = block.toString().substring(6, block.toString().length() - 1);
                blocksJson.add(blockID, extractBlockSounds(block));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return blocksJson;
    }

    private static JsonObject extractBlockSounds(Block block) {
        JsonObject blockJson = new JsonObject();
        JsonObject blockSoundJson = new JsonObject();
        var properties = block.properties();
        SoundType sound = block.defaultBlockState().getSoundType();
        blockSoundJson.addProperty("break", sound.getBreakSound().location().toString());
        blockSoundJson.addProperty("hit", sound.getHitSound().location().toString());
        blockSoundJson.addProperty("fall", sound.getFallSound().location().toString());
        blockSoundJson.addProperty("place", sound.getPlaceSound().location().toString());
        blockSoundJson.addProperty("step", sound.getStepSound().location().toString());
        blockJson.add("sounds", blockSoundJson);
        blockJson.addProperty("blast_resistance", breakField(properties, "explosionResistance", Float.class));
        blockJson.addProperty("hardness", breakField(properties, "destroyTime", Float.class));
        blockJson.addProperty("transparent", !breakField(properties, "canOcclude", Boolean.class));
        blockJson.addProperty("ignited_by_lava", breakField(properties, "ignitedByLava", Boolean.class));
        blockJson.addProperty("stack_size", block.asItem().getDefaultMaxStackSize());
        blockJson.addProperty("map_color", String.format(Locale.ROOT, "#%08X", block.defaultMapColor().calculateARGBColor(MapColor.Brightness.HIGH)));
        return blockJson;
    }

    private static <T> T breakField(Object object, String field, Class<T> resultClass) {
        try {
            var fieldObj = object.getClass().getDeclaredField(field);
            fieldObj.setAccessible(true);
            return resultClass.cast(fieldObj.get(object));
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

}