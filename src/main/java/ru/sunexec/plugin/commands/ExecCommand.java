package ru.sunexec.plugin.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.sunexec.plugin.SunExec;
import ru.sunexec.plugin.config.ConfigManager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ExecCommand implements CommandExecutor {
    private final SunExec plugin;
    private final ConfigManager config;
    private final Map<UUID, Long> cooldowns;
    
    public ExecCommand(SunExec plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfigManager();
        this.cooldowns = new HashMap<>();
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Проверка прав доступа
        if (!hasPermission(sender)) {
            String msg = config.getMessage("no-permission");
            if (msg != null) sender.sendMessage(msg);
            return true;
        }
        
        // Проверка аргументов
        if (args.length == 0) {
            String msg = config.getMessage("usage", "{label}", label);
            if (msg != null) sender.sendMessage(msg);
            return true;
        }
        
        // Проверка кулдауна для игроков
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (!checkCooldown(player)) {
                return true;
            }
        }
        
        // Объединение аргументов в команду
        StringBuilder commandBuilder = new StringBuilder();
        for (String arg : args) {
            commandBuilder.append(arg).append(" ");
        }
        String fullCommand = commandBuilder.toString().trim();
        
        // Проверка длины команды
        if (config.getMaxCommandLength() > 0 && fullCommand.length() > config.getMaxCommandLength()) {
            String msg = config.getMessage("command-error", 
                "{error}", "Команда слишком длинная (максимум " + config.getMaxCommandLength() + " символов)");
            if (msg != null) sender.sendMessage(msg);
            return true;
        }
        
        // Получение базовой команды для проверки
        String baseCommand = args[0].toLowerCase();
        if (baseCommand.startsWith("/")) {
            baseCommand = baseCommand.substring(1);
        }
        
        // Проверка на заблокированные команды
        if (config.isEnableBlockedCommands() && config.getBlockedCommands().contains(baseCommand)) {
            String msg = config.getMessage("command-error", 
                "{error}", "Команда '" + baseCommand + "' заблокирована");
            if (msg != null) sender.sendMessage(msg);
            return true;
        }
        
        // Проверка режима белого списка команд
        if (config.isWhitelistMode() && !config.getAllowedCommands().contains(baseCommand)) {
            String msg = config.getMessage("command-error", 
                "{error}", "Команда '" + baseCommand + "' не разрешена");
            if (msg != null) sender.sendMessage(msg);
            return true;
        }
        
        // Выполнение команды
        try {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), fullCommand);
            
            // Отправка сообщения об успешном выполнении
            if (config.isShowCommandFeedback()) {
                String msg = config.getMessage("command-executed", "{command}", fullCommand);
                if (msg != null) sender.sendMessage(msg);
            }
            
            // Логирование в консоль
            if (config.isLogCommands()) {
                plugin.getLogger().info(sender.getName() + " выполнил команду: " + fullCommand);
            }
            
            // Обновление кулдауна
            if (sender instanceof Player) {
                cooldowns.put(((Player) sender).getUniqueId(), System.currentTimeMillis());
            }
            
        } catch (Exception e) {
            String msg = config.getMessage("command-error", "{error}", e.getMessage());
            if (msg != null) sender.sendMessage(msg);
            return true;
        }
        
        return true;
    }
    
    private boolean hasPermission(CommandSender sender) {
        // Консоль всегда имеет права
        if (!(sender instanceof Player)) {
            return true;
        }
        
        Player player = (Player) sender;
        
        // Проверка белого списка
        if (config.isWhitelistEnabled()) {
            if (config.getWhitelistedPlayers().contains(player.getName()) || 
                config.getWhitelistedPlayers().contains(player.getUniqueId().toString())) {
                return true;
            }
        }
        
        // Проверка стандартного разрешения
        return player.hasPermission(config.getPermission());
    }
    
    private boolean checkCooldown(Player player) {
        UUID uuid = player.getUniqueId();
        
        if (cooldowns.containsKey(uuid)) {
            long lastUse = cooldowns.get(uuid);
            long cooldownTime = config.getCommandCooldown();
            long timePassed = System.currentTimeMillis() - lastUse;
            
            if (timePassed < cooldownTime) {
                long timeLeft = (cooldownTime - timePassed) / 1000;
                String msg = config.getMessage("command-error", 
                    "{error}", "Подождите еще " + timeLeft + " секунд");
                if (msg != null) player.sendMessage(msg);
                return false;
            }
        }
        
        return true;
    }
}