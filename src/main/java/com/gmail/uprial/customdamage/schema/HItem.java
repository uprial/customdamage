package com.gmail.uprial.customdamage.schema;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import com.gmail.uprial.customdamage.ConfigReader;
import com.gmail.uprial.customdamage.common.CustomLogger;

public class HItem {

	private Set<EntityType> sources;
	private Set<EntityType> excluded_sources;
	private Set<EntityType> targets;
	private Set<EntityType> excluded_targets;
	private Set<DamageCause> causes;
	private HItemFormula formula;
	
	public HItem(Set<EntityType> sources, Set<EntityType> excluded_sources, Set<EntityType> targets, Set<EntityType> excluded_targets,
					Set<DamageCause> causes, HItemFormula formula) {
		this.sources = sources;
		this.excluded_sources = excluded_sources;
		this.targets = targets;
		this.excluded_targets = excluded_targets;
		this.causes = causes;
		this.formula = formula;
	}
	
    public double calculateDamageByEntity(double baseDamage, Entity source, Entity target, DamageCause cause) {
    	if (containsSource(source.getType()) && containsTarget(target.getType()) && containsCause(cause))
    		return formula.calculateDamage(baseDamage, target);
    	else
    		return baseDamage;
    }
    
    public double calculateDamage(double baseDamage, Entity target, DamageCause cause) {
    	if (containsTarget(target.getType()) && containsCause(cause))
    		return formula.calculateDamage(baseDamage, target);
    	else
    		return baseDamage;
    }
	
    
    private boolean containsSource(EntityType source) {
    	return ((null == sources) || (sources.contains(source)))
    			&& ((null == excluded_sources) || (! excluded_sources.contains(source)));
    }
    
    private boolean containsTarget(EntityType target) {
    	return ((null == targets) || (targets.contains(target)))
    			&& ((null == excluded_targets) || (! excluded_targets.contains(target)));
    }

    private boolean containsCause(DamageCause cause) {
    	return (null == causes) || (causes.contains(cause));
    }

	public static HItem getFromConfig(FileConfiguration config, CustomLogger customLogger, String key) {
		
		Set<EntityType> sources = getEntityTypesFromConfig(config, customLogger, key + ".sources", "sources list of handler", key);
		Set<EntityType> excluded_sources = getEntityTypesFromConfig(config, customLogger, key + ".excluded-sources", "excluded sources list of handler", key);
		Set<EntityType> targets = getEntityTypesFromConfig(config, customLogger, key + ".targets", "targets list of handler", key);
		Set<EntityType> excluded_targets = getEntityTypesFromConfig(config, customLogger, key + ".excluded-targets", "excluded targets list of handler", key);
		Set<DamageCause> causes = getDamageCausesFromConfig(config, customLogger, key + ".causes", "damage causes list of handler", key);
		
		HItemFormula formula = HItemFormula.getFromConfig(config, customLogger, key);
		if (null == formula)
			return null;
		
		if (formula.hasStatictics()) {
			if ((null == targets) || (! targets.contains(EntityType.PLAYER))) {
				customLogger.error(String.format("Formula of handler '%s' uses a player statistics but handler source does not contain '%s'",
													key, EntityType.PLAYER.toString()));
				return null;
			} else if (targets.size() > 1) {
				customLogger.error(String.format("Formula of handler '%s' uses a player statictics but handler source contains not only '%s'",
													key, EntityType.PLAYER.toString()));
				return null;
				
			}
		}
		if ((null != sources) && (null != excluded_sources)) {
			Set<EntityType> sourcesIntersection = getIntersection(sources, excluded_sources);
			if (sourcesIntersection.size() > 0) {
				customLogger.error(String.format("Sources list and excluded sources list of handler '%s' have conflicting items: %s",
						key, sourcesIntersection.toString()));
				return null;
			}
		}
		if ((null != targets) && (null != excluded_targets)) {
			Set<EntityType> targetsIntersection = getIntersection(targets, excluded_targets);
			if (targetsIntersection.size() > 0) {
				customLogger.error(String.format("Targets list and excluded targets list of handler '%s' have conflicting items: %s",
						key, targetsIntersection.toString()));
				return null;
			}
		}
					
		return new HItem(sources, excluded_sources, targets, excluded_targets, causes, formula);
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