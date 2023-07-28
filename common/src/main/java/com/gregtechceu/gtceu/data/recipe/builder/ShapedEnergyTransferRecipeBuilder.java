package com.gregtechceu.gtceu.data.recipe.builder;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.recipe.ingredient.NBTIngredient;
import com.lowdragmc.lowdraglib.utils.NBTToJsonConverter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

@Accessors(chain = true, fluent = true)
public class ShapedEnergyTransferRecipeBuilder {
    private final List<Ingredient> ingredients = new ArrayList<>();
    @Setter
    protected String group;

    private ItemStack output = ItemStack.EMPTY;
    @Setter
    protected ResourceLocation id;
    @Setter
    protected boolean overrideCharge, transferMaxCharge;
    @Setter
    protected Predicate<ItemStack> chargePredicate;

    public ShapedEnergyTransferRecipeBuilder(@Nullable ResourceLocation id) {
        this.id = id;
    }

    public ShapedEnergyTransferRecipeBuilder requires(TagKey<Item> itemStack) {
        return requires(Ingredient.of(itemStack));
    }

    public ShapedEnergyTransferRecipeBuilder requires(ItemStack itemStack) {
        if (itemStack.hasTag() || itemStack.getDamageValue() >0) {
            requires(NBTIngredient.createNBTIngredient(itemStack));
        }else {
            requires(Ingredient.of(itemStack));
        }
        return this;
    }

    public ShapedEnergyTransferRecipeBuilder requires(ItemLike itemLike) {
        return requires(Ingredient.of(itemLike));
    }

    public ShapedEnergyTransferRecipeBuilder requires(Ingredient ingredient) {
        ingredients.add(ingredient);
        return this;
    }

    public ShapedEnergyTransferRecipeBuilder output(ItemStack itemStack) {
        this.output = itemStack.copy();
        return this;
    }

    public ShapedEnergyTransferRecipeBuilder output(ItemStack itemStack, int count) {
        this.output = itemStack.copy();
        this.output.setCount(count);
        return this;
    }

    public ShapedEnergyTransferRecipeBuilder output(ItemStack itemStack, int count, CompoundTag nbt) {
        this.output = itemStack.copy();
        this.output.setCount(count);
        this.output.setTag(nbt);
        return this;
    }

    protected ResourceLocation defaultId() {
        return BuiltInRegistries.ITEM.getKey(output.getItem());
    }

    public void toJson(JsonObject json) {
        if (group != null) {
            json.addProperty("group", group);
        }

        JsonArray jsonarray = new JsonArray();
        for (Ingredient ingredient : ingredients) {
            jsonarray.add(ingredient.toJson());
        }
        json.add("ingredients", jsonarray);

        if (output.isEmpty()) {
            GTCEu.LOGGER.error("shaped energy transfer recipe {} output is empty", id);
            throw new IllegalArgumentException(id + ": output items is empty");
        } else {
            JsonObject result = new JsonObject();
            result.addProperty("item", BuiltInRegistries.ITEM.getKey(output.getItem()).toString());
            if (output.getCount() > 1) {
                result.addProperty("count", output.getCount());
            }
            if (output.hasTag() && output.getTag() != null) {
                result.add("nbt", NBTToJsonConverter.getObject(output.getTag()));
            }
            json.add("result", result);
        }

        json.addProperty("override_charge", overrideCharge);
        json.addProperty("transfer_max_charge", transferMaxCharge);
        if (chargePredicate instanceof Ingredient ingredient) {
            json.add("charge_predicate", ingredient.toJson());
        }

    }

    public void save(Consumer<FinishedRecipe> consumer) {
        consumer.accept(new FinishedRecipe() {
            @Override
            public void serializeRecipeData(JsonObject pJson) {
                toJson(pJson);
            }

            @Override
            public ResourceLocation getId() {
                var ID = id == null ? defaultId() : id;
                return new ResourceLocation(ID.getNamespace(), "shaped_energy_transfer" + "/" + ID.getPath());
            }

            @Override
            public RecipeSerializer<?> getType() {
                return RecipeSerializer.SHAPELESS_RECIPE;
            }

            @Nullable
            @Override
            public JsonObject serializeAdvancement() {
                return null;
            }

            @Nullable
            @Override
            public ResourceLocation getAdvancementId() {
                return null;
            }
        });
    }
}
