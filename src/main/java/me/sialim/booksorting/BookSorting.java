package me.sialim.booksorting;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public final class BookSorting extends JavaPlugin implements Listener, CommandExecutor {
    private List<UUID> playerList = new ArrayList<>();

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        getCommand("booksort").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (playerList.contains(player.getUniqueId())) {
                playerList.remove(player.getUniqueId());
                player.sendMessage(ChatColor.RED + "Book sorting disabled.");
            } else {
                playerList.add(player.getUniqueId());
                player.sendMessage(ChatColor.GREEN + "Book sorting enabled.");
            }
            return true;
        }
        return false;
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent e) {
        if (playerList.contains(e.getPlayer().getUniqueId())) {
            Inventory inventory = e.getInventory();

            boolean hasOtherItems = false;
            List<ItemStack> books = new ArrayList<>();

            for (ItemStack item : inventory.getContents()) {
                if (item != null) {
                    if (item.getType() == Material.WRITTEN_BOOK || item.getType() == Material.WRITABLE_BOOK) {
                        books.add(item);
                    } else {
                        hasOtherItems = true;
                        break;
                    }
                }
            }

            if (hasOtherItems) {
                e.getPlayer().closeInventory();
                e.getPlayer().sendMessage("Please remove all items other than written books or writeable books before sorting.");
                return;
            }

            books.sort((b1, b2) -> {
                BookMeta meta1 = (BookMeta) b1.getItemMeta();
                BookMeta meta2 = (BookMeta) b2.getItemMeta();

                String content1 = String.join("", meta1.getPages());
                String content2 = String.join("", meta2.getPages());

                int lengthCompare = Integer.compare(content1.length(), content2.length());

                if (lengthCompare == 0) {
                    String title1 = meta1.getTitle() != null ? meta1.getTitle() : "";
                    String title2 = meta2.getTitle() != null ? meta2.getTitle() : "";
                    return title1.compareTo(title2);
                }

                return lengthCompare;
            });

            inventory.clear();
            for (int i = 0; i < books.size(); i++) {
                inventory.setItem(i, books.get(i));
            }
        }
    }
}
