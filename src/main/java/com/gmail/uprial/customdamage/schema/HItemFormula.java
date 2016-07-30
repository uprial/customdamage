package com.gmail.uprial.customdamage.schema;

import com.gmail.uprial.customdamage.common.CustomLogger;
import org.bukkit.Statistic;
import org.bukkit.Statistic.Type;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class HItemFormula {

    private final CustomLogger customLogger;
    private final String handlerName;
    private final String expression;
    private final HashMap<String,Statistic> statistics;

    private static final HashMap<Type,String> FORBIDDEN_STATISTICS = new HashMap<Type,String>() {
        private static final long serialVersionUID = 1L;
    {
        put(Type.BLOCK, "Statistics of this type require a Block Material qualifier");
        put(Type.ENTITY, "Statistics of this type require an EntityType qualifier");
        put(Type.ITEM, "Statistics of this type require an Item Material qualifier");
    }};

    private HItemFormula(CustomLogger customLogger, String handlerName, String expression, HashMap<String, Statistic> statistics) {
        this.customLogger = customLogger;
        this.handlerName = handlerName;
        this.expression = expression;

        this.statistics = !statistics.isEmpty() ? statistics : null;
    }

    public double calculateDamage(double baseDamage, Entity statisticsSource) {
        ScriptEngine jsEngine = getEngine();

        Player player;
        jsEngine.put("$X", baseDamage);

        if (statistics != null) {
            if  (!(statisticsSource instanceof Player)) {
                customLogger.error(String.format("Formula of handler '%s' expected a Player instead of %s",
                        handlerName, statisticsSource.getType().toString()));
                return baseDamage;
            }
            player = (Player)statisticsSource;
            for (HashMap.Entry<String, Statistic> entry : statistics.entrySet()) {
                jsEngine.put('$' + entry.getKey(), player.getStatistic(entry.getValue()));
            }
        } else {
            player = null;
        }

        if (customLogger.isDebugMode()) {
            LinkedHashMap<String, Object> params = new LinkedHashMap<>();
            params.put("baseDamage", String.format("%.2f", baseDamage));

            if (player != null) {
                for (HashMap.Entry<String, Statistic> entry : statistics.entrySet()) {
                    params.put(entry.getKey(), player.getStatistic(entry.getValue()));
                }
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

    public boolean hasStatistics() {
        return (statistics != null);
    }

    private void test() throws ScriptException {
        ScriptEngine jsEngine = getEngine();

        jsEngine.put("$X", 0);
        if (statistics != null) {
            Set<String> keys = statistics.keySet();
            for (String varname : keys) {
                jsEngine.put('$' + varname, 0);
            }
        }
        jsEngine.eval(expression);
    }

    private static ScriptEngine getEngine() {
        ScriptEngineManager mgr = new ScriptEngineManager();
        return mgr.getEngineByName("JavaScript");
    }


    public static HItemFormula getFromConfig(FileConfiguration config, CustomLogger customLogger, String key, String handlerName) {
        String expression = config.getString(key);

        if(expression == null) {
            customLogger.error(String.format("Null/Empty formula of handler '%s'", handlerName));
            return null;
        }

        HashMap<String,Statistic> statistics = new HashMap<>();

        Boolean error = false;
        Boolean xFound = false;

        Matcher matcher = Pattern.compile("\\$(\\w+)").matcher(expression);
        //noinspection MethodCallInLoopCondition
        while (matcher.find()) {
            String varname = matcher.group(1);
            if (varname.equals("X")) {
                xFound = true;
            } else {
                Statistic statistic;
                try {
                    statistic = Statistic.valueOf(varname);
                } catch (IllegalArgumentException ignored) {
                    customLogger.error(String.format("Invalid statistic '%s' in formula of handler '%s'", varname, handlerName));
                    error = true;
                    continue;
                }
                if (statistic.getType() != Type.UNTYPED) {
                    String errorMessage = getForbiddenStatisticsErrorMessage(statistic.getType());
                    List<Statistic> allowedStatistics = getAllowedStatistics();
                    customLogger.error(String.format("Forbidden statistic '%s' in formula of handler '%s': %s. Allowed statistics: %s",
                                                        varname, handlerName, errorMessage, allowedStatistics.toString()));
                    error = true;
                    continue;
                }

                statistics.put(varname, statistic);
            }
        }
        if (!xFound) {
            customLogger.warning(String.format("Formula of handler '%s' does no contain $X variable", handlerName));
        }
        if (error) {
            return null;
        }

        HItemFormula formula = new HItemFormula(customLogger, handlerName, expression, statistics);
        try {
            formula.test();
        } catch (ScriptException ex) {
            customLogger.error(String.format("Formula of handler '%s' can not be evaluated: %s", handlerName, ex.getMessage()));
            return null;
        }

        return formula;
    }

    private static String getForbiddenStatisticsErrorMessage(Type statisticType) {
        String errorMessage;
        errorMessage = FORBIDDEN_STATISTICS.containsKey(statisticType)
                ? FORBIDDEN_STATISTICS.get(statisticType)
                : statisticType.toString();

        return errorMessage;
    }

    private static List<Statistic> getAllowedStatistics() {
        List<Statistic> allowedStatistics = new ArrayList<>();
        for(Statistic item : Statistic.values()) {
            if (item.getType() == Type.UNTYPED) {
                allowedStatistics.add(item);
            }
        }

        return allowedStatistics;
    }

}
