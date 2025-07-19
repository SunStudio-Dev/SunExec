package ru.sunexec.plugin.config;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import ru.sunexec.plugin.SunExec;

import java.util.*;

public class ConfigManager {
    private final SunExec plugin;
    
    // Кешированные значения конфигурации
    private String prefix;
    private String permission;
    private boolean logCommands;
    private List<String> aliases;
    private boolean whitelistEnabled;
    private Set<String> whitelistedPlayers;
    private Map<String, String> messages;
    private boolean enableBlockedCommands;
    private Set<String> blockedCommands;
    private boolean whitelistMode;
    private boolean enableWhitelistMode;
    private Set<String> allowedCommands;
    private long commandCooldown;
    private int maxCommandLength;
    private boolean showCommandFeedback;
    
    public ConfigManager(SunExec plugin) {
        this.plugin = plugin;
        this.messages = new HashMap<>();
        this.whitelistedPlayers = new HashSet<>();
        this.blockedCommands = new HashSet<>();
        this.allowedCommands = new HashSet<>();
    }
    
    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();
        
        // Загрузка основных настроек
        prefix = colorize(config.getString("settings.prefix", "&6[&eSunExec&6]&r"));
        permission = config.getString("settings.permission", "sunexec.use");
        logCommands = config.getBoolean("settings.log-commands", true);
        
        // Загрузка алиасов
        aliases = config.getStringList("aliases");
        
        // Загрузка белого списка игроков
        whitelistEnabled = config.getBoolean("whitelist.enabled", false);
        whitelistedPlayers.clear();
        whitelistedPlayers.addAll(config.getStringList("whitelist.players"));
        
        // Загрузка сообщений
        messages.clear();
        if (config.isConfigurationSection("messages")) {
            for (String key : config.getConfigurationSection("messages").getKeys(false)) {
                messages.put(key, colorize(config.getString("messages." + key)));
            }
        }
        
        // Загрузка настроек безопасности
        enableBlockedCommands = config.getBoolean("security.enable-blocked-commands", true);
        blockedCommands.clear();
        if (enableBlockedCommands) {
            for (String cmd : config.getStringList("security.blocked-commands")) {
                blockedCommands.add(cmd.toLowerCase());
            }
        }
        
        whitelistMode = config.getBoolean("security.whitelist-mode", false);
        enableWhitelistMode = config.getBoolean("security.enable-whitelist-mode", false);
        
        allowedCommands.clear();
        if (enableWhitelistMode && whitelistMode) {
            for (String cmd : config.getStringList("security.allowed-commands")) {
                allowedCommands.add(cmd.toLowerCase());
            }
        }
        
        // Загрузка дополнительных настроек
        commandCooldown = config.getLong("advanced.command-cooldown", 1000);
        maxCommandLength = config.getInt("advanced.max-command-length", 0);
        showCommandFeedback = config.getBoolean("advanced.show-command-feedback", true);
    }
    
    public String getMessage(String key, String... replacements) {
        String message = messages.get(key);
        
        // Если сообщение пустое или null, возвращаем null
        if (message == null || message.trim().isEmpty()) {
            return null;
        }
        
        message = prefix + " " + message;
        
        // Замена плейсхолдеров
        if (replacements.length > 0) {
            for (int i = 0; i < replacements.length; i += 2) {
                if (i + 1 < replacements.length) {
                    message = message.replace(replacements[i], replacements[i + 1]);
                }
            }
        }
        
        return message;
    }
    
    private String colorize(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
    
    // Геттеры для кешированных значений
    public String getPrefix() { return prefix; }
    public String getPermission() { return permission; }
    public boolean isLogCommands() { return logCommands; }
    public List<String> getAliases() { return aliases; }
    public boolean isWhitelistEnabled() { return whitelistEnabled; }
    public Set<String> getWhitelistedPlayers() { return whitelistedPlayers; }
    public boolean isEnableBlockedCommands() { return enableBlockedCommands; }
    public Set<String> getBlockedCommands() { return blockedCommands; }
    public boolean isWhitelistMode() { return whitelistMode && enableWhitelistMode; }
    public Set<String> getAllowedCommands() { return allowedCommands; }
    public long getCommandCooldown() { return commandCooldown; }
    public int getMaxCommandLength() { return maxCommandLength; }
    public boolean isShowCommandFeedback() { return showCommandFeedback; }
}