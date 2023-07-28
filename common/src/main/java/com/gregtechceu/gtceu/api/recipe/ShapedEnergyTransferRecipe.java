package com.gregtechceu.gtceu.api.recipe;

import com.google.gson.JsonObject;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IElectricItem;
import com.gregtechceu.gtceu.api.item.GTToolItem;
import com.gregtechceu.gtceu.api.item.capability.ElectricItem;
import com.gregtechceu.gtceu.core.mixins.ShapedRecipeInvoker;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;


@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ShapedEnergyTransferRecipe extends ShapedRecipe {
    public static final RecipeSerializer<ShapedEnergyTransferRecipe> SERIALIZER = new Serializer();

    private final Predicate<ItemStack> chargePredicate;
    private final boolean overrideCharge, transferMaxCharge;

    public ShapedEnergyTransferRecipe(ResourceLocation id, String group, int width, int height, NonNullList<Ingredient> recipeItems, ItemStack result, Predicate<ItemStack> chargePredicate, boolean overrideCharge, boolean transferMaxCharge, Object... recipe) {
        super(id, group, CraftingBookCategory.EQUIPMENT, width, height, recipeItems, result);
        this.chargePredicate = chargePredicate;
        this.overrideCharge = overrideCharge;
        this.transferMaxCharge = transferMaxCharge;
        if (overrideCharge) {
            fixOutputItemMaxCharge();
        }
    }

    //transfer initial max charge for correct display in JEI
    private void fixOutputItemMaxCharge() {
        //noinspection ConstantValue
        long totalMaxCharge = getIngredients().stream()
                .mapToLong(it -> Arrays.stream(it.getItems())
                        .filter(itemStack -> !(itemStack.getItem() instanceof GTToolItem))
                        .map(stack -> GTCapabilityHelper.getElectricItem(stack.copy()))
                        .filter(Objects::nonNull)
                        .mapToLong(IElectricItem::getMaxCharge)
                        .max().orElse(0L)).sum();
        IElectricItem electricItem = GTCapabilityHelper.getElectricItem(this.result);
        if (totalMaxCharge > 0L && electricItem instanceof ElectricItem) {
            ((ElectricItem) electricItem).setMaxChargeOverride(totalMaxCharge);
        }
    }

    @Override
    public ItemStack assemble(CraftingContainer craftingContainer, RegistryAccess registryAccess) {
        ItemStack resultStack = this.getResultItem(registryAccess).copy();
        chargeStackFromComponents(resultStack, craftingContainer, chargePredicate, transferMaxCharge);
        return resultStack;
    }

    public static void chargeStackFromComponents(ItemStack toolStack, Container ingredients, Predicate<ItemStack> chargePredicate, boolean transferMaxCharge) {
        IElectricItem electricItem = GTCapabilityHelper.getElectricItem(toolStack);
        long totalMaxCharge = 0L;
        long toCharge = 0L;
        if (electricItem != null && electricItem.getMaxCharge() > 0L) {
            for (int slotIndex = 0; slotIndex < ingredients.getContainerSize(); slotIndex++) {
                ItemStack stackInSlot = ingredients.getItem(slotIndex);
                if (!chargePredicate.test(stackInSlot)) {
                    continue;
                }
                IElectricItem batteryItem = GTCapabilityHelper.getElectricItem(stackInSlot);
                if (batteryItem == null) {
                    continue;
                }
                totalMaxCharge += batteryItem.getMaxCharge();
                toCharge += batteryItem.discharge(Long.MAX_VALUE, Integer.MAX_VALUE, true, true, true);
            }
        }
        if (electricItem instanceof ElectricItem && transferMaxCharge) {
            ((ElectricItem) electricItem).setMaxChargeOverride(totalMaxCharge);
        }
        electricItem.charge(toCharge, Integer.MAX_VALUE, true, false);
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }

    public static class Serializer implements RecipeSerializer<ShapedEnergyTransferRecipe> {
        @Override
        public ShapedEnergyTransferRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            String string = GsonHelper.getAsString(json, "group", "");
            Map<String, Ingredient> map = ShapedRecipeInvoker.callKeyFromJson(GsonHelper.getAsJsonObject(json, "key"));
            String[] strings = ShapedRecipeInvoker.callShrink(ShapedRecipeInvoker.callPatternFromJson(GsonHelper.getAsJsonArray(json, "pattern")));
            int width = strings[0].length();
            int height = strings.length;
            NonNullList<Ingredient> ingredients = ShapedRecipeInvoker.callDissolvePattern(strings, map, width, height);
            ItemStack result = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));

            boolean overrideCharge = GsonHelper.getAsBoolean(json, "override_charge", true);
            boolean transferMaxCharge = GsonHelper.getAsBoolean(json, "transfer_max_charge", true);

            Predicate<ItemStack> filter = stack -> true;
            if (json.has("charge_predicate")) {
                filter = Ingredient.fromJson(json.get("charge_predicate"));
            }

            return new ShapedEnergyTransferRecipe(recipeId, string, width, height, ingredients, result, filter, overrideCharge, transferMaxCharge);
        }

        @Override
        public ShapedEnergyTransferRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            int i = buffer.readVarInt();
            int j = buffer.readVarInt();
            String string = buffer.readUtf();
            NonNullList<Ingredient> nonNullList = NonNullList.withSize(i * j, Ingredient.EMPTY);
            nonNullList.replaceAll(ignored -> Ingredient.fromNetwork(buffer));
            ItemStack itemStack = buffer.readItem();
            boolean overrideCharge = buffer.readBoolean();
            boolean transferMaxCharge = buffer.readBoolean();
            Predicate<ItemStack> filter = stack -> true;
            if (buffer.readBoolean()) {
                filter = Ingredient.fromNetwork(buffer);
            }
            return new ShapedEnergyTransferRecipe(recipeId, string, i, j, nonNullList, itemStack, filter, overrideCharge, transferMaxCharge);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, ShapedEnergyTransferRecipe recipe) {
            buffer.writeVarInt(recipe.getWidth());
            buffer.writeVarInt(recipe.getHeight());
            buffer.writeUtf(recipe.getGroup());
            buffer.writeEnum(recipe.category());
            for (Ingredient ingredient : recipe.getIngredients()) {
                ingredient.toNetwork(buffer);
            }
            buffer.writeItem(recipe.result);
            buffer.writeBoolean(recipe.overrideCharge);
            buffer.writeBoolean(recipe.transferMaxCharge);

            if (recipe.chargePredicate instanceof Ingredient ingredient) {
                buffer.writeBoolean(true);
                ingredient.toNetwork(buffer);
            } else {
                buffer.writeBoolean(false);
            }
        }
    }
}
