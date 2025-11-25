package vv0ta3fa9.plugin.kItemsInfo.commands;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import vv0ta3fa9.plugin.kItemsInfo.KItemsInfo;

public class CommandManager implements CommandExecutor {

    private final KItemsInfo plugin;

    public CommandManager(KItemsInfo plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            send(sender, "&cЭта команда доступна только игрокам!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            send(sender, "&cИспользование: /kitemsinfo info <all|modeldata>");
            send(sender, "&cИспользование: /kitemsinfo setmodeldata <число>");
            return true;
        }

        String subcommand = args[0].toLowerCase();

        if (subcommand.equals("info")) {
            if (args.length < 2) {
                send(sender, "&cИспользование: /kitemsinfo info <all|modeldata>");
                return true;
            }

            ItemStack item = player.getInventory().getItemInMainHand();
            if (item == null || item.getType() == Material.AIR) {
                send(sender, "&cВы должны держать предмет в руке!");
                return true;
            }

            String infoType = args[1].toLowerCase();
            if (infoType.equals("all")) {
                showAllNBT(player, item);
            } else if (infoType.equals("modeldata")) {
                showModelData(player, item);
            } else {
                send(sender, "&cНеизвестный тип информации. Используйте: all или modeldata");
            }
            return true;
        }

        if (subcommand.equals("setmodeldata")) {
            if (args.length < 2) {
                send(sender, "&cИспользование: /kitemsinfo setmodeldata <число>");
                return true;
            }

            ItemStack item = player.getInventory().getItemInMainHand();
            if (item == null || item.getType() == Material.AIR) {
                send(sender, "&cВы должны держать предмет в руке!");
                return true;
            }

            try {
                int modelData = Integer.parseInt(args[1]);
                setModelData(item, modelData);
                player.getInventory().setItemInMainHand(item);
                send(sender, "&aModelData установлен: &e" + modelData);
            } catch (NumberFormatException e) {
                send(sender, "&cОшибка: &e" + args[1] + " &cне является числом!");
            }
            return true;
        }

        send(sender, "&cНеизвестная подкоманда. Используйте: info или setmodeldata");
        return true;
    }

    private void showAllNBT(Player player, ItemStack item) {
        send(player, "&6=== Все NBT теги предмета ===");

        String nbtString = getNBTAsString(item);
        if (nbtString != null && !nbtString.isEmpty()) {
            send(player, "&eNBT: &f" + nbtString);
        } else {
            send(player, "&cНе удалось получить NBT данные");
        }

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            if (meta.hasDisplayName()) {
                send(player, "&eDisplayName: &f" + meta.getDisplayName());
            }
            if (meta.hasLore()) {
                send(player, "&eLore: &f" + String.join(", ", meta.getLore()));
            }
            if (meta.hasCustomModelData()) {
                send(player, "&eCustomModelData: &f" + meta.getCustomModelData());
            }
        }

        send(player, "&eMaterial: &f" + item.getType().name());
        send(player, "&eAmount: &f" + item.getAmount());
    }
    
    private String getNBTAsString(ItemStack item) {
        try {
            Object nmsItem = getNMSItemStack(item);
            if (nmsItem == null) return null;
            
            Object nbtTag = nmsItem.getClass().getMethod("getTag").invoke(nmsItem);
            if (nbtTag == null) return "{}";
            
            return nbtTag.toString();
        } catch (Exception e) {
            return null;
        }
    }
    
    private Object getNMSItemStack(ItemStack item) {
        try {
            Class<?> craftItemStackClass = Class.forName("org.bukkit.craftbukkit." + getServerVersion() + ".inventory.CraftItemStack");
            Object craftItemStack = craftItemStackClass.getMethod("asNMSCopy", ItemStack.class).invoke(null, item);
            return craftItemStack;
        } catch (Exception e) {
            return null;
        }
    }
    
    private String getServerVersion() {
        String packageName = plugin.getServer().getClass().getPackage().getName();
        return packageName.substring(packageName.lastIndexOf('.') + 1);
    }

    private void showModelData(Player player, ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null && meta.hasCustomModelData()) {
            send(player, "&aModelData предмета: &e" + meta.getCustomModelData());
        } else {
            send(player, "&cУ предмета нет ModelData (CustomModelData)");
        }
    }

    private void setModelData(ItemStack item, int modelData) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setCustomModelData(modelData);
            item.setItemMeta(meta);
        }
    }

    private void send(CommandSender sender, String msg) {
        sender.sendMessage(plugin.getColorizer().colorize(msg));
    }
}

