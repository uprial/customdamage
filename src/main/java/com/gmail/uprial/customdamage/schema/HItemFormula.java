package com.gmail.uprial.customdamage.schema;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.bukkit.Statistic;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.gmail.uprial.customdamage.common.CustomLogger;

public class HItemFormula {

	private final CustomLogger customLogger;
	private final String handlerName;
	private final String expression;
	private final HashMap<String,Statistic> statistics;
	
	private static final HashMap<Statistic.Type,String> forbinnedStatistics = new HashMap<Statistic.Type,String>() {
		private static final long serialVersionUID = 1L;
	{
		put(Statistic.Type.BLOCK, "Statistics of this type require a Block Material qualifier");
		put(Statistic.Type.ENTITY, "Statistics of this type require an EntityType qualifier");
		put(Statistic.Type.ITEM, "Statistics of this type require an Item Material qualifier");
	}};
	
	public HItemFormula(CustomLogger customLogger, String handlerName, String expression, HashMap<String,Statistic> statistics) {
		this.customLogger = customLogger;
		this.handlerName = handlerName;
		this.expression = expression;
		
		if (statistics.size() > 0)
			this.statistics = statistics;
		else
			this.statistics = null;
	}
	
    public double calculateDamage(double baseDamage, Entity statisticsSource) {
	    ScriptEngine jsEngine = getEngine();
	    
	    Player player;
		jsEngine.put("$X", baseDamage);
	    
	    if (null != statistics) {
	    	if  (!(statisticsSource instanceof Player)) {
				customLogger.error(String.format("Formula of handler '%s' expected a Player instead of %s",
						handlerName, statisticsSource.getType().toString()));
				return baseDamage;
	    	}
	    	player = (Player)statisticsSource;
	    	for (HashMap.Entry<String, Statistic> entry : statistics.entrySet())
	    		jsEngine.put("$" + entry.getKey(), player.getStatistic(entry.getValue()));
	    } else
	    	player = null;
	    
	    if (customLogger.isDebugMode()) {
	    	LinkedHashMap<String, Object> params = new LinkedHashMap<String, Object>();
		    params.put("baseDamage", String.format("%.2f", baseDamage));
		    
		    if (null != player) {
		    	for (HashMap.Entry<String, Statistic> entry : statistics.entrySet())
					params.put(entry.getKey(), player.getStatistic(entry.getValue()));
		    }
	    	
	    	customLogger.debug(String.format("Apply %s%s", handlerName, params.toString()));
	    }
	    
   		try {
			return Float.parseFloat(jsEngine.eval(expression).toString());
		} catch (NumberFormatException ex) {
			customLogger.error(String.format("Formula of handler '%s' can not be evaluated: %s", handlerName, ex.getMessage()));
			return baseDamage;
		} catch (ScriptException ex) {
			customLogger.error(String.format("Formula of handler '%s' can not be converted to a float: %s", handlerName, ex.getMessage()));
			return baseDamage;
		}
    }
	
	
	public boolean hasStatictics() {
		return (null != statistics);
	}
	
	public void test() throws ScriptException {
	    ScriptEngine jsEngine = getEngine();
	    
	    jsEngine.put("$X", 0);
	    if (null != statistics) {
			Set<String> keys = statistics.keySet();
			for (String varname : keys) {
				jsEngine.put("$" + varname, 0);
			}
	    }
    	jsEngine.eval(expression);
	}

	private ScriptEngine getEngine() {
	    ScriptEngineManager mgr = new ScriptEngineManager();
	    return mgr.getEngineByName("JavaScript");
	}
	
	
	public static HItemFormula getFromConfig(FileConfiguration config, CustomLogger customLogger, String key, String handlerName) {
		String expression = config.getString(key);
		
		if(null == expression) {
			customLogger.error(String.format("Null/Empty formula of handler '%s'", handlerName));
			return null;
		}

		HashMap<String,Statistic> statistics = new HashMap<String,Statistic>();
		
		Boolean error = false;
		Boolean x_found = false;
		
		Matcher matcher = Pattern.compile("\\$(\\w+)").matcher(expression);
		while (matcher.find()) {
			String varname = matcher.group(1);
			if (varname.equals("X")) {
				x_found = true;
			} else {
				Statistic statistic;
				try {
					statistic = Statistic.valueOf(varname);
				} catch (java.lang.IllegalArgumentException e) {
					customLogger.error(String.format("Invalid statistic '%s' in formula of handler '%s'", varname, handlerName));
					error = true;
					continue;
				}
				if (statistic.getType() != Statistic.Type.UNTYPED) {
					String errorMessage = getForbinnedStatisticsErrorMessage(statistic.getType());
					List<Statistic> allowedStatistics = getAllowedStatistics(); 
					customLogger.error(String.format("Forbidden statistic '%s' in formula of handler '%s': %s. Allowed statistics: %s",
														varname, handlerName, errorMessage, allowedStatistics.toString()));
					error = true;
					continue;
				}
				
				statistics.put(varname, statistic);
			}
		}
		if (!x_found) {
			customLogger.warning(String.format("Formula of handler '%s' does no contain $X variable", handlerName));
		}
		if (error)
			return null;
		
		HItemFormula formula = new HItemFormula(customLogger, handlerName, expression, statistics);
		try {
			formula.test();
	    } catch (ScriptException ex) {
			customLogger.error(String.format("Formula of handler '%s' can not be evaluated: %s", handlerName, ex.getMessage()));
			return null;
	    }
		
		return formula;
	}
	
	private static String getForbinnedStatisticsErrorMessage(Statistic.Type statisticType) {
		String errorMessage;
		if (forbinnedStatistics.containsKey(statisticType))
			errorMessage = forbinnedStatistics.get(statisticType);
		else
			errorMessage = statisticType.toString();
		
		return errorMessage;
	}

	private static List<Statistic> getAllowedStatistics() {
		List<Statistic> allowedStatistics = new ArrayList<Statistic>(); 
		for(Statistic item : Statistic.values())
			if (item.getType() == Statistic.Type.UNTYPED)
				allowedStatistics.add(item);
		
		return allowedStatistics;
	}

}