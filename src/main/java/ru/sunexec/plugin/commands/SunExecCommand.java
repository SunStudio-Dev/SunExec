package ru.sunexec.plugin.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import ru.sunexec.plugin.SunExec;
import ru.sunexec.plugin.config.ConfigManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SunExecCommand implements CommandExecutor, TabCompleter {
    private final SunExec plugin;
    private final ConfigManager config;
    
    public SunExecCommand(SunExec plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfigManager();
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("sunexec.admin")) {
            String msg = config.getMessage("no-permission");
            if (msg != null) sender.sendMessage(msg);
            return true;
        }
        
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "reload":
                reloadConfig(sender);
                break;
            case "help":
                sendHelp(sender);
                break;
            case "info":
                sendInfo(sender);
                break;
            default:
                String msg = config.getMessage("command-error", 
                    "{error}", "Неизвестная подкоманда: " + subCommand);
                if (msg != null) sender.sendMessage(msg);
                sendHelp(sender);
                break;
        }
        
        return true;
    }
    
    private void reloadConfig(CommandSender sender) {
        try {
            config.loadConfig();
            plugin.reregisterCommands();
            String msg = config.getMessage("config-reloaded");
            if (msg != null) sender.sendMessage(msg);
        } catch (Exception e) {
            String msg = config.getMessage("command-error", 
                "{error}", "Ошибка при перезагрузке конфигурации: " + e.getMessage());
            if (msg != null) sender.sendMessage(msg);
        }
    }
    
    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§6§m--------§r §e§lSunExec §6§m--------");
        sender.sendMessage("§e/sunexec reload §7- Перезагрузить конфигурацию");
        sender.sendMessage("§e/sunexec info §7- Информация о плагине");
        sender.sendMessage("§e/sunexec help §7- Показать эту справку");
        sender.sendMessage("§6§m------------------------");
    }
    
    private void sendInfo(CommandSender sender) {
        sender.sendMessage("§6§m--------§r §e§lSunExec Info §6§m--------");
        sender.sendMessage("§eВерсия: §f" + plugin.getDescription().getVersion());
        sender.sendMessage("§eАвтор: §f" + plugin.getDescription().getAuthors().get(0));
        sender.sendMessage("§eСлоган: §f" + plugin.getDescription().getDescription());
        sender.sendMessage("§e");
        sender.sendMessage("§eСтатистика:");
        sender.sendMessage("§7• Алиасов команды: §f" + config.getAliases().size());
        sender.sendMessage("§7• Заблокированных команд: §f" + config.getBlockedCommands().size());
        sender.sendMessage("§7• Игроков в белом списке: §f" + config.getWhitelistedPlayers().size());
        sender.sendMessage("§6§m--------------------------------");
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("sunexec.admin")) {
            return new ArrayList<>();
        }
        
        if (args.length == 1) {
            List<String> completions = Arrays.asList("reload", "info", "help");
            List<String> result = new ArrayList<>();
            
            for (String completion : completions) {
                if (completion.toLowerCase().startsWith(args[0].toLowerCase())) {
                    result.add(completion);
                }
            }
            
            return result;
        }
        
        return new ArrayList<>();
    }
}