package tk.davizin.resetmine;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;

import tk.davizin.pombaminerar.cache.Cache;

public class ResetMine extends JavaPlugin implements CommandExecutor {
	public static ResetMine m;
	public static WorldEditPlugin we;

	public void onEnable() {
		m = this;
		try {
			carregarConfig();
			registerTasks();
			registerCommands();
			registerWorldEdit();
			c("&aPlugin iniciado com sucesso");
			return;
		} catch (Exception e) {
			c("&cOcorreu um erro ao iniciar o plugin");
			e.printStackTrace();
			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}
	}

	private void registerWorldEdit() {
		Plugin p = Bukkit.getPluginManager().getPlugin("WorldEdit");
		if (p instanceof WorldEditPlugin)
			we = (WorldEditPlugin) p;
		c("&aPlugin associado com o 'WorldEdit' com sucesso.");
	}

	private void registerCommands() {
		getCommand("setmina").setExecutor(this);
		c("&aComandos registrados com sucesso");
	}

	private void registerTasks() {
		new BukkitRunnable() {
			@Override
			public void run() {				
				resetarMina();
			}
		}.runTaskTimer(this, 20*60*3, 20 * 60 * 3);
		c("&aTasks registradas com sucesso");
	}

	public boolean resetando = false;

	private void resetarMina() {
		for (String mina : getConfig().getConfigurationSection("Minas").getKeys(false)) {
			Material m = Material.valueOf(getConfig().getString("Minas." + mina + ".Bloco"));
			if (Bukkit.getOnlinePlayers().size() > 0) {
				resetando = true;
				double top = getConfig().getDouble("Minas." + mina + ".Pos1.Y");
				for (Location loc : getMina(mina)) {
					if (loc.getBlock().getType() != m)
						loc.getBlock().setType(m);
					for (Entity ent : loc.getWorld().getNearbyEntities(loc, 1.0, 0.0, 1.0)) {
						if (ent instanceof Player) {
							Player p = (Player)ent;
							Cache.namina.remove(p);
							p.teleport(new Location(loc.getWorld(), p.getLocation().getX(), top + 2,
									p.getLocation().getZ()));
							Cache.namina.add(p);
							p.sendMessage("§e§lMINAS §fA mina §e" + mina + "§f foi resetada");
						}
					}
				}				
				resetando = false;
			}
		}
	}

	private void setMina(Location l, String mina, String pos) {		
		getConfig().set("Minas." + mina + ".Mundo", l.getWorld().getName());
		getConfig().set("Minas." + mina + ".Porcentagem", 40);
		getConfig().set("Minas." + mina + ".Bloco", "LAPIS_ORE");
		getConfig().set("Minas." + mina + "." + pos + ".X", l.getX());
		getConfig().set("Minas." + mina + "." + pos + ".Y", l.getY());
		getConfig().set("Minas." + mina + "." + pos + ".Z", l.getZ());
		saveConfig();
	}

	private List<Location> getMina(String mina) {
		World w = Bukkit.getWorld(getConfig().getString("Minas." + mina + ".Mundo"));
		Location loc1 = new Location(w, getConfig().getDouble("Minas." + mina + ".Pos1.X"),
				getConfig().getDouble("Minas." + mina + ".Pos1.Y"), getConfig().getDouble("Minas." + mina + ".Pos1.Z"));
		Location loc2 = new Location(w, getConfig().getDouble("Minas." + mina + ".Pos2.X"),
				getConfig().getDouble("Minas." + mina + ".Pos2.Y"), getConfig().getDouble("Minas." + mina + ".Pos2.Z"));
		return getLocation(loc1, loc2);
	}

	public static void c(String msg) {
		Bukkit.getConsoleSender().sendMessage("§a[" + m.getDescription().getName() + " "
				+ m.getDescription().getVersion() + "] " + msg.replace("&", "§"));
	}

	@Override
	public boolean onCommand(CommandSender s, Command c, String l, String[] args) {
		if (!(s instanceof Player))
			return false;
		Player p = (Player) s;
		if (!p.hasPermission("pomba.setmina"))
			return false;
		if (args.length < 1) {
			p.sendMessage("§cUtilize /setmina <nome>");
			return false;
		}
		if (we.getSelection(p) == null) {
			p.sendMessage("§cSelecione a area da mina primeiro");
			return false;
		}
		setMina(we.getSelection(p).getMaximumPoint(), args[0], "Pos1");
		setMina(we.getSelection(p).getMinimumPoint(), args[0], "Pos2");
		p.sendMessage("§aA mina '" + args[0] + "' foi setada com sucesso.");
		return false;
	}

	public static List<Location> getLocation(Location loc1, Location loc2) {
		double xMin = Math.min(loc1.getX(), loc2.getX());
		double yMin = Math.min(loc1.getY(), loc2.getY());
		double zMin = Math.min(loc1.getZ(), loc2.getZ());
		double xMax = Math.max(loc1.getX(), loc2.getX());
		double yMax = Math.max(loc1.getY(), loc2.getY());
		double zMax = Math.max(loc1.getZ(), loc2.getZ());
		List<Location> locs = new ArrayList<>();
		World world = loc1.getWorld();
		for (double x = xMin; x <= xMax; x++) {
			for (double y = yMin; y <= yMax; y++) {
				for (double z = zMin; z <= zMax; z++) {
					locs.add(new Location(world, x, y, z));
				}
			}
		}
		return locs;
	}

	private void carregarConfig() {
		File f = new File(getDataFolder(), "config.yml");
		if (!f.exists())
			saveDefaultConfig();
		c("&aConfiguração 'config.yml' criada/carregada com sucesso");
	}

}
