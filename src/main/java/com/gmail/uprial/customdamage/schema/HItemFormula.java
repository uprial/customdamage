package com.gmail.uprial.customdamage.schema;

import com.gmail.uprial.customdamage.common.CustomLogger;
import com.gmail.uprial.customdamage.config.InvalidConfigException;
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

final class HItemFormula {
    private static final String BASE_VAR_PREDICATE = "$";
    private static final String BASE_VAR_NAME = "X";

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

    double calculateDamage(double baseDamage, Entity statisticsSource) {
        ScriptEngine jsEngine = getEngine();

        Player player;
        jsEngine.put(BASE_VAR_PREDICATE + BASE_VAR_NAME, baseDamage);

        if (statistics != null) {
            if  (!(statisticsSource instanceof Player)) {
                customLogger.error(String.format("Formula of handler '%s' expected a Player instead of %s",
                        handlerName, statisticsSource.getType().toString()));
                return baseDamage;
            }
            player = (Player)statisticsSource;
            for (HashMap.Entry<String, Statistic> entry : statistics.entrySet()) {
                jsEngine.put(BASE_VAR_PREDICATE + entry.getKey(), player.getStatistic(entry.getValue()));
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

    boolean hasStatistics() {
        return (statistics != null);
    }

    private void test() throws ScriptException {
        ScriptEngine jsEngine = getEngine();

        jsEngine.put(BASE_VAR_PREDICATE + BASE_VAR_NAME, 0);
        if (statistics != null) {
            Set<String> keys = statistics.keySet();
            for (String varName : keys) {
                jsEngine.put(BASE_VAR_PREDICATE + varName, 0);
            }
        }
        jsEngine.eval(expression);
    }

    private static ScriptEngine getEngine() {
        ScriptEngineManager mgr = new ScriptEngineManager();
        return mgr.getEngineByName("JavaScript");
    }


    static HItemFormula getFromConfig(FileConfiguration config, CustomLogger customLogger, String key, String handlerName) throws InvalidConfigException {
        String expression = config.getString(key);

        if(expression == null) {
            throw new InvalidConfigException(String.format("Null/Empty formula of handler '%s'", handlerName));
        }

        HashMap<String,Statistic> statistics = new HashMap<>();

        boolean xFound = false;

        Matcher matcher = Pattern.compile("\\" + BASE_VAR_PREDICATE + "(\\w+)").matcher(expression);
        //noinspection MethodCallInLoopCondition
        while (matcher.find()) {
            String varName = matcher.group(1);
            if (varName.equals(BASE_VAR_NAME)) {
                xFound = true;
            } else {
                Statistic statistic;
                try {
                    statistic = Statistic.valueOf(varName);
                } catch (IllegalArgumentException ignored) {
                    throw new InvalidConfigException(String.format("Invalid statistic '%s' in formula of handler '%s'", varName, handlerName));
                }
                if (statistic.getType() != Type.UNTYPED) {
                    String errorMessage = getForbiddenStatisticsErrorMessage(statistic.getType());
                    List<Statistic> allowedStatistics = getAllowedStatistics();
                    throw new InvalidConfigException(String.format("Forbidden statistic '%s' in formula of handler '%s': %s. Allowed statistics: %s",
                                                        varName, handlerName, errorMessage, allowedStatistics.toString()));
                }

                statistics.put(varName, statistic);
            }
        }
        if (!xFound) {
            customLogger.warning(String.format("Formula of handler '%s' does no contain %s%s variable",
                    handlerName, BASE_VAR_PREDICATE, BASE_VAR_NAME));
        }
        HItemFormula formula = new HItemFormula(customLogger, handlerName, expression, statistics);
        try {
            formula.test();
        } catch (ScriptException ex) {
            throw new InvalidConfigException(String.format("Formula of handler '%s' can not be evaluated: %s", handlerName, ex.getMessage()));
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
