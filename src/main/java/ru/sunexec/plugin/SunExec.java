package ru.sunexec.plugin;

import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.plugin.java.JavaPlugin;
import ru.sunexec.plugin.commands.ExecCommand;
import ru.sunexec.plugin.commands.ExecTabCompleter;
import ru.sunexec.plugin.commands.SunExecCommand;
import ru.sunexec.plugin.config.ConfigManager;

import java.lang.reflect.Field;
import java.util.List;

public class SunExec extends JavaPlugin {
    private ConfigManager configManager;
    private ExecCommand execCommand;
    private ExecTabCompleter execTabCompleter;
    private SunExecCommand sunExecCommand;
    
    @Override
    public void onEnable() {
        // Инициализация конфига
        configManager = new ConfigManager(this);
        configManager.loadConfig();
        
        // Инициализация команд
        execCommand = new ExecCommand(this);
        execTabCompleter = new ExecTabCompleter(this);
        sunExecCommand = new SunExecCommand(this);
        
        // Регистрация команд
        registerCommands();
        
        // Красивое сообщение о запуске
        getLogger().info("╔═══════════════════════════════════════════════════════════════════╗");
        getLogger().info("║                           SunExec v" + getDescription().getVersion() + "                            ║");
        getLogger().info("║         Консоль в кармане - управляй сервером одной рукой!        ║");
        getLogger().info("║                      Плагин успешно загружен!                      ║");
        getLogger().info("╚═══════════════════════════════════════════════════════════════════╝");
    }
    
    @Override
    public void onDisable() {
        getLogger().info("SunExec отключен. Спасибо за использование!");
    }
    
    private void registerCommands() {
        // Регистрация основных команд
        getCommand("exec").setExecutor(execCommand);
        getCommand("exec").setTabCompleter(execTabCompleter);
        getCommand("sunexec").setExecutor(sunExecCommand);
        getCommand("sunexec").setTabCompleter(sunExecCommand);
        
        // Регистрация алиасов
        registerAliases();
    }
    
    private void registerAliases() {
        try {
            // Получаем CommandMap через рефлексию
            Field commandMapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            CommandMap commandMap = (CommandMap) commandMapField.get(Bukkit.getServer());
            
            // Регистрируем каждый алиас с префиксом плагина
            for (String alias : configManager.getAliases()) {
                try {
                    // Формируем имя команды с префиксом плагина
                    String pluginAliasName = "sunexec:" + alias;
                    
                    // Проверяем, не занята ли команда
                    if (commandMap.getCommand(pluginAliasName) != null) {
                        getLogger().warning("Команда '" + pluginAliasName + "' уже существует, пропускаем регистрацию алиаса");
                        continue;
                    }
                    
                    // Создаем новую команду для алиаса
                    AliasCommand aliasCommand = new AliasCommand(pluginAliasName, this);
                    aliasCommand.setExecutor(execCommand);
                    aliasCommand.setTabCompleter(execTabCompleter);
                    aliasCommand.setDescription("Выполнить команду от имени консоли");
                    aliasCommand.setUsage("/" + pluginAliasName + " <команда>");
                    aliasCommand.setPermission(configManager.getPermission());
                    
                    // Регистрируем команду с префиксом плагина
                    commandMap.register("sunexec", aliasCommand);
                    getLogger().info("Зарегистрирован алиас: /" + pluginAliasName);
                } catch (Exception e) {
                    getLogger().warning("Не удалось зарегистрировать алиас '" + alias + "': " + e.getMessage());
                }
            }
        } catch (Exception e) {
            getLogger().severe("Ошибка при регистрации алиасов: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Внутренний класс для создания команд-алиасов
    private static class AliasCommand extends Command {
        private CommandExecutor executor;
        private TabCompleter tabCompleter;
        
        public AliasCommand(String name, JavaPlugin plugin) {
            super(name);
        }
        
        @Override
        public boolean execute(CommandSender sender, String commandLabel, String[] args) {
            if (executor != null) {
                return executor.onCommand(sender, this, commandLabel, args);
            }
            return false;
        }
        
        @Override
        public List<String> tabComplete(CommandSender sender, String alias, String[] args) throws IllegalArgumentException {
            if (tabCompleter != null) {
                return tabCompleter.onTabComplete(sender, this, alias, args);
            }
            return super.tabComplete(sender, alias, args);
        }
        
        public void setExecutor(CommandExecutor executor) {
            this.executor = executor;
        }
        
        public void setTabCompleter(TabCompleter tabCompleter) {
            this.tabCompleter = tabCompleter;
        }
    }
    
    public void reregisterCommands() {
        registerAliases();
    }
    
    public ConfigManager getConfigManager() {
        return configManager;
    }
}