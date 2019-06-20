package com.gmail.uprial.customdamage.schema;

import com.gmail.uprial.customdamage.config.ConfigReader;
import com.gmail.uprial.customdamage.common.CustomLogger;
import com.gmail.uprial.customdamage.config.InvalidConfigException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class HItem {

    private final Set<EntityType> sources;
    private final Set<EntityType> excludedSources;
    private final Set<EntityType> targets;
    private final Set<EntityType> excludedTargets;
    private final Set<DamageCause> causes;
    private final Set<DamageCause> excludedCauses;
    private final String userInfo;
    private final boolean useSourceStatistics;
    private final HItemFormula formula;

    private HItem(Set<EntityType> sources, Set<EntityType> excludedSources, Set<EntityType> targets, Set<EntityType> excludedTargets,
                  Set<DamageCause> causes, Set<DamageCause> excludedCauses, String userInfo,
                  boolean useSourceStatistics, HItemFormula formula) {
        this.sources = sources;
        this.excludedSources = excludedSources;
        this.targets = targets;
        this.excludedTargets = excludedTargets;
        this.causes = causes;
        this.excludedCauses = excludedCauses;
        this.userInfo = userInfo;
        this.useSourceStatistics = useSourceStatistics;
        this.formula = formula;
    }

    public double calculateDamageByEntity(double baseDamage, Entity source, Entity target, DamageCause cause) {
        if (containsSource(source.getType()) && containsTarget(target.getType()) && containsCause(cause)) {
            return useSourceStatistics
                    ? formula.calculateDamage(baseDamage, source)
                    : formula.calculateDamage(baseDamage, target);
        } else {
            return baseDamage;
        }
    }

    public double calculateDamage(double baseDamage, Entity target, DamageCause cause) {
        return (containsSource(EntityType.UNKNOWN) && containsTarget(target.getType()) && containsCause(cause))
                ? formula.calculateDamage(baseDamage, target)
                : baseDamage;
    }

    public boolean isUserVisible() {
        return (userInfo != null);
    }

    public String getPlayerInfo(Player player) {
        return String.format(userInfo, formula.calculateDamage(1, player));
    }

    private boolean containsSource(EntityType source) {
        return containsItem(sources, excludedSources, source);
    }

    private boolean containsTarget(EntityType target) {
        return containsItem(targets, excludedTargets, target);
    }

    private boolean containsCause(DamageCause cause) {
        return containsItem(causes, excludedCauses, cause);
    }

    private static <T> boolean containsItem(Set<T> set, Set<T> excludedSet, T item) {
        return ((set == null) || (set.contains(item)))
                && ((excludedSet == null) || (! excludedSet.contains(item)));
    }

    public static HItem getFromConfig(FileConfiguration config, CustomLogger customLogger, String key) throws InvalidConfigException {

        Set<EntityType> sources = getEntityTypesFromConfig(config, customLogger, key + ".sources", String.format("sources list of handler '%s'", key));
        Set<EntityType> excludedSources = getEntityTypesFromConfig(config, customLogger, key + ".excluded-sources", String.format("excluded sources list of handler '%s'", key));
        Set<EntityType> targets = getEntityTypesFromConfig(config, customLogger, key + ".targets", String.format("targets list of handler '%s'", key));
        Set<EntityType> excludedTargets = getEntityTypesFromConfig(config, customLogger, key + ".excluded-targets", String.format("excluded targets list of handler '%s'", key));
        Set<DamageCause> causes = getDamageCausesFromConfig(config, customLogger, key + ".causes", String.format("damage causes list of handler '%s'", key));
        Set<DamageCause> excludedCauses = getDamageCausesFromConfig(config, customLogger, key + ".excluded-causes", String.format("excluded damage causes list of handler '%s'", key));

        testIntersection(customLogger, key, "Sources list and excluded sources list", sources, excludedSources);
        testIntersection(customLogger, key, "Targets list and excluded targets list", targets, excludedTargets);
        testIntersection(customLogger, key, "Causes list and excluded causes list", causes, excludedCauses);

        boolean useSourceStatistics = ConfigReader.getBoolean(config, customLogger, key + ".use-source-statistics", String.format("use-source-statistics flag of handler '%s'", key), false);

        HItemFormula formula = HItemFormula.getFromConfig(config, customLogger, key + ".formula", key);

        if (formula.hasStatistics()) {
            Set<EntityType> items;
            String itemsTitle;
            if (useSourceStatistics) {
                items = sources;
                itemsTitle = "sources";
            }
            else {
                items = targets;
                itemsTitle = "targets";
            }

            if ((items == null) || (! items.contains(EntityType.PLAYER))) {
                throw new InvalidConfigException(String.format("Formula of handler '%s' uses a player statistics but handler '%s' do not contain '%s'",
                        key, itemsTitle, EntityType.PLAYER.toString()));
            } else if (items.size() > 1) {
                throw new InvalidConfigException(String.format("Formula of handler '%s' uses a player statistics but handler '%s' contain not only '%s'",
                        key, itemsTitle, EntityType.PLAYER.toString()));
            }
        }

        String userInfo = ConfigReader.getStringUnsafe(config, customLogger, key + ".user-info", String.format("user-info string of handler '%s'", key));

        return new HItem(sources, excludedSources, targets, excludedTargets, causes, excludedCauses, userInfo, useSourceStatistics, formula);
    }

    private static <T> void testIntersection(CustomLogger customLogger, String key, String title, Set<T> setA, Set<T> setB) throws InvalidConfigException {
        if ((setA != null) && (setB != null)) {
            Set<T> intersection = getIntersection(setA, setB);
            if (!intersection.isEmpty()) {
                throw new InvalidConfigException(String.format("%s of handler '%s' have conflicting items: %s", title, key, intersection.toString()));
            }
        }
    }

    private static Set<EntityType> getEntityTypesFromConfig(FileConfiguration config, CustomLogger customLogger, String key, String title) throws InvalidConfigException {
        List<String> strings = ConfigReader.getStringList(config, customLogger, key, title);
        if (strings == null) {
            return null;
        }

        List<EntityType> entityTypeList = getProjectileEntityTypes();
        Set<EntityType> entityTypes = new HashSet<>();
        int stringsSize = strings.size();
        for(int i = 0; i < stringsSize; i++) {
            String string = strings.get(i);
            EntityType entityType;
            try {
                entityType = EntityType.valueOf(string);
            } catch (IllegalArgumentException ignored) {
                throw new InvalidConfigException(String.format("Invalid entity type '%s' in %s at pos %d", string, title, i));
            }
            if (entityTypes.contains(entityType)) {
                throw new InvalidConfigException(String.format("Entity type '%s' in %s is not unique", entityType.toString(), title));
            }
            if (entityTypeList.contains(entityType)) {
                throw new InvalidConfigException(String.format("Entity type '%s' in %s should not be a projectile. Projectile entity types: %s",
                                    entityType.toString(), title, entityTypeList.toString()));
            }
            entityTypes.add(entityType);
        }
        return entityTypes;
    }

    private static Set<DamageCause> getDamageCausesFromConfig(FileConfiguration config, CustomLogger customLogger, String key, String title) throws InvalidConfigException {
        List<String> strings = ConfigReader.getStringList(config, customLogger, key, title);
        if (strings == null) {
            return null;
        }

        Set<DamageCause> damageCauses = new HashSet<>();
        int stringsSize = strings.size();
        for(int i = 0; i < stringsSize; i++) {
            String string = strings.get(i);
            DamageCause damageCause;
            try {
                damageCause = DamageCause.valueOf(string);
            } catch (IllegalArgumentException ignored) {
                throw new InvalidConfigException(String.format("Invalid damage cause '%s' in %s at pos %d", string, title, i));
            }
            if (damageCauses.contains(damageCause)) {
                throw new InvalidConfigException(String.format("Damage cause '%s' in %s is not unique", damageCause.toString(), title));
            }
            damageCauses.add(damageCause);
        }

        return damageCauses;
    }

    private static List<EntityType> getProjectileEntityTypes() {
        List<EntityType> entityTypeList = new ArrayList<>();
        for(EntityType item : EntityType.values()) {
            Class<? extends Entity> entityClass = item.getEntityClass();
            if ((entityClass != null) && (Projectile.class.isAssignableFrom(entityClass))) {
                entityTypeList.add(item);
            }
        }

        return entityTypeList;
    }

    private static <T> Set<T> getIntersection(Set<T> setA, Set<T> setB) {
        Set<T> intersection = new HashSet<>();
        for (T item : setA) {
            if (setB.contains(item)) {
                intersection.add(item);
            }
        }

        return intersection;
    }

}
