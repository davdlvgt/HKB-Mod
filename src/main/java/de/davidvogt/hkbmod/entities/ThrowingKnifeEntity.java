package de.davidvogt.hkbmod.entities;

import de.davidvogt.hkbmod.item.ModItems;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Collection;

public class ThrowingKnifeEntity extends ThrowableItemProjectile {

    // EntityData für Client-Server Synchronisation
    private static final EntityDataAccessor<Float> DATA_DAMAGE =
            SynchedEntityData.defineId(ThrowingKnifeEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> DATA_EXPLOSIVE =
            SynchedEntityData.defineId(ThrowingKnifeEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_RETURNING =
            SynchedEntityData.defineId(ThrowingKnifeEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_PIERCING =
            SynchedEntityData.defineId(ThrowingKnifeEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_BLOCK_BREAKING =
            SynchedEntityData.defineId(ThrowingKnifeEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> DATA_KNOCKBACK =
            SynchedEntityData.defineId(ThrowingKnifeEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<String> DATA_POTION =
            SynchedEntityData.defineId(ThrowingKnifeEntity.class, EntityDataSerializers.STRING);

    // Lokale Variablen für erweiterte Funktionalität
    private Player originalOwner;
    private int returningTimer = 0;
    private int pierceCount = 0;
    private final int maxPierceCount = 3;

    public ThrowingKnifeEntity(EntityType<? extends ThrowingKnifeEntity> entityType, Level level) {
        super(entityType, level);
        initializeDefaults();
    }

    public ThrowingKnifeEntity(Level level, LivingEntity shooter) {
        super(ModEntityTypes.THROWING_KNIFE.get(), level);
        this.setOwner(shooter);
        if (shooter instanceof Player player) {
            this.originalOwner = player;
        }
        initializeDefaults();
    }

    private void initializeDefaults() {
        this.entityData.set(DATA_DAMAGE, 4.0F);
        this.entityData.set(DATA_EXPLOSIVE, false);
        this.entityData.set(DATA_RETURNING, false);
        this.entityData.set(DATA_PIERCING, false);
        this.entityData.set(DATA_BLOCK_BREAKING, false);
        this.entityData.set(DATA_KNOCKBACK, 0.0F);
        this.entityData.set(DATA_POTION, "minecraft:water");
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_DAMAGE, 4.0F);
        builder.define(DATA_EXPLOSIVE, false);
        builder.define(DATA_RETURNING, false);
        builder.define(DATA_PIERCING, false);
        builder.define(DATA_BLOCK_BREAKING, false);
        builder.define(DATA_KNOCKBACK, 0.0F);
        builder.define(DATA_POTION, "minecraft:water");
    }

    @Override
    public void tick() {
        super.tick();

        // Rückkehr-Logik
        if (isReturning() && originalOwner != null && !originalOwner.isRemoved()) {
            returningTimer++;
            if (returningTimer > 60) { // Nach 3 Sekunden kehrt es zurück
                Vec3 toOwner = originalOwner.position().subtract(this.position()).normalize();
                this.setDeltaMovement(toOwner.scale(0.5));

                // Prüfe ob es den Besitzer erreicht hat
                if (this.distanceTo(originalOwner) < 1.5) {
                    // Gib das Item zurück
                    originalOwner.getInventory().add(this.getItem());
                    this.discard();
                }
            }
        }

        // Partikeleffekte basierend auf Typ
        if (level().isClientSide) {
            spawnTrailParticles();
        }
    }

    private void spawnTrailParticles() {
        if (isExplosive()) {
            level().addParticle(ParticleTypes.SMOKE,
                    getX(), getY(), getZ(), 0, 0, 0);
        } else if (!getImbuedPotion().equals(Potions.WATER)) {
            level().addParticle(ParticleTypes.WITCH,
                    getX(), getY(), getZ(), 0, 0, 0);
        }
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.THROWING_KNIFE.get();
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        Entity target = result.getEntity();

        if (target instanceof LivingEntity livingTarget) {
            float damage = getBaseDamage();

            // Schaden zufügen (nur auf Server-Seite)
            boolean hurt = false;
            if (level() instanceof ServerLevel serverLevel) {
                hurt = livingTarget.hurtServer(serverLevel, this.damageSources().thrown(this, this.getOwner()), damage);
            }

            if (hurt) {
                // Knockback anwenden
                if (getKnockback() > 0) {
                    Vec3 knockbackVec = this.getDeltaMovement().normalize().scale(getKnockback());
                    livingTarget.push(knockbackVec.x, Math.max(knockbackVec.y, 0.1), knockbackVec.z);
                }

                // Trank-Effekte anwenden
                applyPotionEffects(livingTarget);

                // Partikel-Effekte
                spawnHitParticles(target);
            }

            // Piercing-Logik
            if (isPiercing() && pierceCount < maxPierceCount) {
                pierceCount++;
                return; // Nicht stoppen, weiterfliegen
            }
        }

        // Explosions-Logik
        if (isExplosive()) {
            createExplosion();
        }

        // Sound-Effekt
        level().playSound(null, getX(), getY(), getZ(),
                SoundEvents.ARROW_HIT, SoundSource.NEUTRAL, 1.0F, 1.0F);

        // Entity entfernen (außer bei Rückkehr)
        if (!level().isClientSide && !isReturning()) {
            discard();
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);

        BlockState blockState = level().getBlockState(result.getBlockPos());

        // Block-Breaking Logik
        if (canBreakBlocks() && canBreakBlock(blockState.getBlock())) {
            level().destroyBlock(result.getBlockPos(), true);
            return; // Weiterfliegen nach Block-Zerstörung
        }

        // Explosions-Logik
        if (isExplosive()) {
            createExplosion();
        }

        // Sound-Effekt
        level().playSound(null, getX(), getY(), getZ(),
                SoundEvents.ARROW_HIT, SoundSource.NEUTRAL, 1.0F, 1.2F);

        // Entity entfernen (außer bei Rückkehr)
        if (!level().isClientSide && !isReturning()) {
            discard();
        }
    }

    private boolean canBreakBlock(Block block) {
        // Definiere welche Blöcke zerstört werden können
        return block == Blocks.GLASS ||
                block == Blocks.GLASS_PANE ||
                block == Blocks.ICE ||
                block.defaultDestroyTime() < 2.0F; // Weiche Blöcke
    }

    private void applyPotionEffects(LivingEntity target) {
        Holder<Potion> potion = getImbuedPotion();
        if (potion != null && !potion.equals(Potions.WATER)) {
            Collection<MobEffectInstance> effects = potion.value().getEffects();
            for (MobEffectInstance effect : effects) {
                target.addEffect(new MobEffectInstance(effect));
            }
        }
    }

    private void spawnHitParticles(Entity target) {
        if (isExplosive()) {
            level().addParticle(ParticleTypes.EXPLOSION,
                    target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ(),
                    0.0, 0.0, 0.0);
        } else {
            level().addParticle(ParticleTypes.CRIT,
                    target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ(),
                    0.0, 0.0, 0.0);
        }
    }

    private void createExplosion() {
        if (!level().isClientSide) {
            // Kleine Explosion ohne Terrain-Schaden
            level().explode(this, getX(), getY(), getZ(), 2.0F, Level.ExplosionInteraction.NONE);
        }
    }

    // Getter und Setter für alle Eigenschaften
    public float getBaseDamage() {
        return entityData.get(DATA_DAMAGE);
    }

    public void setBaseDamage(float damage) {
        entityData.set(DATA_DAMAGE, damage);
    }

    public boolean isExplosive() {
        return entityData.get(DATA_EXPLOSIVE);
    }

    public void setExplosive(boolean explosive) {
        entityData.set(DATA_EXPLOSIVE, explosive);
    }

    public boolean isReturning() {
        return entityData.get(DATA_RETURNING);
    }

    public void setReturning(boolean returning) {
        entityData.set(DATA_RETURNING, returning);
    }

    public boolean isPiercing() {
        return entityData.get(DATA_PIERCING);
    }

    public void setPiercing(boolean piercing) {
        entityData.set(DATA_PIERCING, piercing);
    }

    public boolean canBreakBlocks() {
        return entityData.get(DATA_BLOCK_BREAKING);
    }

    public void setBlockBreaking(boolean blockBreaking) {
        entityData.set(DATA_BLOCK_BREAKING, blockBreaking);
    }

    public float getKnockback() {
        return entityData.get(DATA_KNOCKBACK);
    }

    public void setKnockback(float knockback) {
        entityData.set(DATA_KNOCKBACK, knockback);
    }

    public Holder<Potion> getImbuedPotion() {
        String potionString = entityData.get(DATA_POTION);
        if (potionString.equals("minecraft:water")) {
            return Potions.WATER;
        }

        // Bekannte Tränke direkt zurückgeben
        return switch (potionString) {
            case "minecraft:poison" -> Potions.POISON;
            case "minecraft:healing" -> Potions.HEALING;
            case "minecraft:harming" -> Potions.HARMING;
            case "minecraft:swiftness" -> Potions.SWIFTNESS;
            case "minecraft:slowness" -> Potions.SLOWNESS;
            case "minecraft:strength" -> Potions.STRENGTH;
            case "minecraft:weakness" -> Potions.WEAKNESS;
            case "minecraft:regeneration" -> Potions.REGENERATION;
            case "minecraft:fire_resistance" -> Potions.FIRE_RESISTANCE;
            case "minecraft:night_vision" -> Potions.NIGHT_VISION;
            case "minecraft:invisibility" -> Potions.INVISIBILITY;
            default -> Potions.WATER; // Fallback
        };
    }

    public void setImbuedPotion(Holder<Potion> potion) {
        String potionKey = BuiltInRegistries.POTION.getKey(potion.value()).toString();
        entityData.set(DATA_POTION, potionKey);
    }

    public void setOriginalOwner(Player owner) {
        this.originalOwner = owner;
    }

    // NBT Serialization für Speichern/Laden
    @Override
    public void addAdditionalSaveData(net.minecraft.world.level.storage.ValueOutput pOutput) {
        super.addAdditionalSaveData(pOutput);
        pOutput.putFloat("BaseDamage", getBaseDamage());
        pOutput.putBoolean("Explosive", isExplosive());
        pOutput.putBoolean("Returning", isReturning());
        pOutput.putBoolean("Piercing", isPiercing());
        pOutput.putBoolean("BlockBreaking", canBreakBlocks());
        pOutput.putFloat("Knockback", getKnockback());
        pOutput.putString("ImbuedPotion", entityData.get(DATA_POTION));
        pOutput.putInt("ReturningTimer", returningTimer);
        pOutput.putInt("PierceCount", pierceCount);
    }

    @Override
    public void readAdditionalSaveData(net.minecraft.world.level.storage.ValueInput pInput) {
        super.readAdditionalSaveData(pInput);
        setBaseDamage(pInput.getFloatOr("BaseDamage", 4.0F));
        setExplosive(pInput.getBooleanOr("Explosive", false));
        setReturning(pInput.getBooleanOr("Returning", false));
        setPiercing(pInput.getBooleanOr("Piercing", false));
        setBlockBreaking(pInput.getBooleanOr("BlockBreaking", false));
        setKnockback(pInput.getFloatOr("Knockback", 0.0F));
        entityData.set(DATA_POTION, pInput.getStringOr("ImbuedPotion", "minecraft:water"));
        returningTimer = pInput.getIntOr("ReturningTimer", 0);
        pierceCount = pInput.getIntOr("PierceCount", 0);
    }
}