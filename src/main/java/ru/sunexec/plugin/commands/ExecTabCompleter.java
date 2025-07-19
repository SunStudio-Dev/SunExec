package ru.sunexec.plugin.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import ru.sunexec.plugin.SunExec;

import java.util.ArrayList;
import java.util.List;

public class ExecTabCompleter implements TabCompleter {
    private final SunExec plugin;
    
    public ExecTabCompleter(SunExec plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        // Возвращаем пустой список - никакого автодополнения
        return new ArrayList<>();
    }
}