package me.HybridPlague.PocketGuide;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin implements Listener {


	Map<String, Long> cooldowns = new HashMap<String, Long>();
	
	@Override
	public void onEnable() {
		this.getServer().getPluginManager().registerEvents(this, this);
		this.saveDefaultConfig();
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		Player p = (Player) e.getPlayer();
		if (!p.hasPlayedBefore()) {
			ItemStack item = new ItemStack(Material.WRITTEN_BOOK, 1);
			item = getConfig().getItemStack("PocketGuideBook.item");
			p.getInventory().setItem(8, item);
			p.getInventory().setHeldItemSlot(8);
		}
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (label.equalsIgnoreCase("pocketguide")) {
			if (!(sender instanceof Player)) {
				return false;
			}
			
			Player p = (Player) sender;
			
			switch(args.length) {
			case 1:
				if (args[0].equalsIgnoreCase("set")) {
					if (!p.hasPermission("pocketguide.set")) {
						p.sendMessage(ChatColor.RED + "Insufficient permissions");
						return true;
					}
					try {
						if (p.getInventory().getItemInMainHand().getType() != Material.WRITTEN_BOOK) {
							p.sendMessage(ChatColor.RED + "That is not a written book!");
							return true;
						}
						this.getConfig().set("PocketGuideBook.item", p.getInventory().getItemInMainHand());
						this.saveConfig();
						this.reloadConfig();
						p.sendMessage(ChatColor.GREEN + "PocketGuide has been saved! You can obtain the item using /PocketGuide get");
						return true;
					} catch (IllegalArgumentException ex) {
						p.sendMessage(ChatColor.RED + "Item cannot be NULL");
						return true;
					}
				}
				if (args[0].equalsIgnoreCase("get")) {
					
					if (cooldowns.containsKey(p.getName())) {
						if (cooldowns.get(p.getName()) > System.currentTimeMillis()) {
							long timeleft = (cooldowns.get(p.getName()) - System.currentTimeMillis()) / 1000;
							p.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "» " + ChatColor.WHITE + "This command has a cooldown. Please wait " + ChatColor.YELLOW + "" + timeleft + ChatColor.WHITE + " more seconds.");
							return true;
						}
					}
					cooldowns.put(p.getName(), System.currentTimeMillis() + (600 * 1000));
					
					ItemStack item = new ItemStack(Material.WRITTEN_BOOK, 1);
					item = getConfig().getItemStack("PocketGuideBook.item");
					if (p.getInventory().firstEmpty() == -1) {
						World world = p.getWorld();
						world.dropItemNaturally(p.getLocation(), item);
						p.sendMessage(ChatColor.GREEN + "Could not find an empty slot, so your PocketGuide has been dropped at your feet.");
						return true;
					}
					p.getInventory().addItem(item);
					p.sendMessage(ChatColor.GREEN + "PocketGuide added to your inventory.");
					return true;
				}
			default:
				if (p.hasPermission("pocketguide.set")) {
					p.sendMessage(ChatColor.RED + "/PocketGuide <get | set>");
					return true;
				}
				p.sendMessage(ChatColor.RED + "/PocketGuide get");
				return true;
			}
		}
		
		return false;
	}
	
}
