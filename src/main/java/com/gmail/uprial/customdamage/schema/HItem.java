package com.gmail.uprial.customdamage.schema;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import com.gmail.uprial.customdamage.ConfigReader;
import com.gmail.uprial.customdamage.common.CustomLogger;

public class HItem {

	private final Set<EntityType> sources;
	private final Set<EntityType> excluded_sources;
	private final Set<EntityType> targets;
	private final Set<EntityType> excluded_targets;
	private final Set<DamageCause> causes;
	private final Set<DamageCause> excluded_causes;
	private final String user_info;
	private final boolean useSourceStatistics;
	private final HItemFormula formula;
	
	public HItem(Set<EntityType> sources, Set<EntityType> excluded_sources, Set<EntityType> targets, Set<EntityType> excluded_targets,
					Set<DamageCause> causes, Set<DamageCause> excluded_causes, String user_info,
					boolean useSourceStatistics, HItemFormula formula) {
		this.sources = sources;
		this.excluded_sources = excluded_sources;
		this.targets = targets;
		this.excluded_targets = excluded_targets;
		this.causes = causes;
		this.excluded_causes = excluded_causes;
		this.user_info = user_info;
		this.useSourceStatistics = useSourceStatistics;
		this.formula = formula;
	}
	
    public double calculateDamageByEntity(double baseDamage, Entity source, Entity target, DamageCause cause) {
    	if (containsSource(source.getType()) && containsTarget(target.getType()) && containsCause(cause)) {
    		if (useSourceStatistics)
        		return formula.calculateDamage(baseDamage, source);
    		else
    			return formula.calculateDamage(baseDamage, target);
    	} else
    		return baseDamage;
    }
    
    public double calculateDamage(double baseDamage, Entity target, DamageCause cause) {
    	if (containsSource(EntityType.UNKNOWN) && containsTarget(target.getType()) && containsCause(cause))
    		return formula.calculateDamage(baseDamage, target);
    	else
    		return baseDamage;
    }
	
    public boolean isUserVisible() {
    	return (null != user_info);
    }
    
    public String getPlayerInfo(Player player) {
    	return String.format(user_info, formula.calculateDamage(1, player));
    }

    private boolean containsSource(EntityType source) {
    	return containsItem(sources, excluded_sources, source);
    }
    
    private boolean containsTarget(EntityType target) {
    	return containsItem(targets, excluded_targets, target);
    }

    private boolean containsCause(DamageCause cause) {
    	return containsItem(causes, excluded_causes, cause);
    }
    
    private <T> boolean containsItem(Set<T> set, Set<T> excluded_set, T item) {
    	return ((null == set) || (set.contains(item)))
    			&& ((null == excluded_set) || (! excluded_set.contains(item)));
    }

	public static HItem getFromConfig(FileConfiguration config, CustomLogger customLogger, String key) {
		
		Set<EntityType> sources = getEntityTypesFromConfig(config, customLogger, key + ".sources", "sources list of handler", key);
		Set<EntityType> excluded_sources = getEntityTypesFromConfig(config, customLogger, key + ".excluded-sources", "excluded sources list of handler", key);
		Set<EntityType> targets = getEntityTypesFromConfig(config, customLogger, key + ".targets", "targets list of handler", key);
		Set<EntityType> excluded_targets = getEntityTypesFromConfig(config, customLogger, key + ".excluded-targets", "excluded targets list of handler", key);
		Set<DamageCause> causes = getDamageCausesFromConfig(config, customLogger, key + ".causes", "damage causes list of handler", key);
		Set<DamageCause> excluded_causes = getDamageCausesFromConfig(config, customLogger, key + ".excluded-causes", "excluded damage causes list of handler", key);
		
		if (hasIntersection(customLogger, key, "Sources list and excluded sources list", sources, excluded_sources))
			return null;
		if (hasIntersection(customLogger, key, "Targets list and excluded targets list", targets, excluded_targets))
			return null;
		if (hasIntersection(customLogger, key, "Causes list and excluded causes list", causes, excluded_causes))
			return null;

		boolean useSourceStatistics = ConfigReader.getBoolean(config, customLogger, key + ".use-source-statistics", "use-source-statistics flag of handler", key, false);
		
		HItemFormula formula = HItemFormula.getFromConfig(config, customLogger, key + ".formula", key);
		if (null == formula)
			return null;
		
		if (formula.hasStatictics()) {
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
			
			if ((null == items) || (! items.contains(EntityType.PLAYER))) {
				customLogger.error(String.format("Formula of handler '%s' uses a player statistics but handler '%s' do not contain '%s'",
						key, itemsTitle, EntityType.PLAYER.toString()));
				return null;
			} else if (items.size() > 1) {
				customLogger.error(String.format("Formula of handler '%s' uses a player statictics but handler '%s' contain not only '%s'",
						key, itemsTitle, EntityType.PLAYER.toString()));
				return null;
				
			}
		}

		String user_info = ConfigReader.getString(config, customLogger, key + ".user-info", "user-info string of handler", key);
					
		return new HItem(sources, excluded_sources, targets, excluded_targets, causes, excluded_causes, user_info, useSourceStatistics, formula);
	}

	private static <T> boolean hasIntersection(CustomLogger customLogger, String key, String title, Set<T> setA, Set<T> setB) {
		if ((null != setA) && (null != setB)) {
			Set<T> intersection = getIntersection(setA, setB);
			if (intersection.size() > 0) {
				customLogger.error(String.format("%s of handler '%s' have conflicting items: %s", title, key, intersection.toString()));
				return true;
			}
		}
		
		return false;
	}

	private static Set<EntityType> getEntityTypesFromConfig(FileConfiguration config, CustomLogger customLogger, String key, String title, String name) {
		List<String> strings = ConfigReader.getStringList(config, customLogger, key, title, name);
		if (null == strings)
			return null;
		
		List<EntityType> projectileEntityTypes = getProjectileEntityTypes();
		Set<EntityType> entityTypes = new HashSet<EntityType>();
		for(int i = 0; i < strings.size(); i++) {
			String string = strings.get(i);
			EntityType entityType;
			try {
				entityType = EntityType.valueOf(string);
			} catch (java.lang.IllegalArgumentException e) {
				customLogger.error(String.format("Invalid entity type '%s' in %s '%s' at pos %d", string, title, name, i));
				continue;
			}
			if (entityTypes.contains(entityType)) {
				customLogger.error(String.format("Entity type '%s' in %s '%s' is not unique", entityType.toString(), title, name, i));
				continue;
			}
			if (projectileEntityTypes.contains(entityType)) {
				customLogger.error(String.format("Entity type '%s' in %s '%s' should not be a projectile. Projectile entity types: %s",
									entityType.toString(), title, name, projectileEntityTypes.toString()));
				continue;
			}
			entityTypes.add(entityType);
		}
		return entityTypes;
	}
	
	private static Set<DamageCause> getDamageCausesFromConfig(FileConfiguration config, CustomLogger customLogger, String key, String title, String name) {
		List<String> strings = ConfigReader.getStringList(config, customLogger, key, title, name);
		if (null == strings)
			return null;
		
		Set<DamageCause> damageCauses = new HashSet<DamageCause>();
		for(int i = 0; i < strings.size(); i++) {
			String string = strings.get(i);
			DamageCause damageCause;
			try {
				damageCause = DamageCause.valueOf(string);
			} catch (java.lang.IllegalArgumentException e) {
				customLogger.error(String.format("Invalid damage cause '%s' in %s '%s' at pos %d", string, title, name, i));
				continue;
			}
			if (damageCauses.contains(damageCause)) {
				customLogger.error(String.format("Damage cause '%s' in %s '%s' is not unique", damageCause.toString(), title, name, i));
				continue;
			}
			damageCauses.add(damageCause);
		}
			
		return damageCauses;
	}
	
	private static List<EntityType> getProjectileEntityTypes() {
		List<EntityType> projectileEntityTypes = new ArrayList<EntityType>(); 
		for(EntityType item : EntityType.values()) {
		    Class<? extends Entity> entityClass = item.getEntityClass();
			if ((null != entityClass) && (Projectile.class.isAssignableFrom(entityClass)))
				projectileEntityTypes.add(item);
		}
		
		return projectileEntityTypes;
	}
	
	private static <T> Set<T> getIntersection(Set<T> setA, Set<T> setB) {
		Set<T> intersection = new HashSet<T>();
		for (T item : setA)
			if (setB.contains(item))
				intersection.add(item);
		
		return intersection;
	}	

}