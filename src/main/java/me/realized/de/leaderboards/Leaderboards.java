package me.realized.de.leaderboards;

import java.util.logging.Level;
import lombok.Getter;
import me.realized.duels.api.arena.ArenaManager;
import me.realized.duels.api.extension.DuelsExtension;
import me.realized.duels.api.kit.KitManager;
import me.realized.duels.api.user.UserManager;
import org.bukkit.event.Listener;

public class Leaderboards extends DuelsExtension implements Listener {

    @Getter
    private UserManager userManager;
    @Getter
    private KitManager kitManager;
    @Getter
    private ArenaManager arenaManager;

    @Override
    public void onEnable() {
        try {
            Class.forName("org.bukkit.entity.ArmorStand");
        } catch (ClassNotFoundException ex) {
            api.getLogger().log(Level.SEVERE, getName() + " could not enable properly because ArmorStand was not supported on this server version.", ex);
            return;
        }

        this.userManager = api.getUserManager();
        this.kitManager = api.getKitManager();
        this.arenaManager = api.getArenaManager();

        api.getServer().getPluginManager().registerEvents(this, api);
    }

    @Override
    public String getRequiredVersion() {
        return "3.1.0";
    }

//    @EventHandler
//    public void on(final PlayerToggleSneakEvent event) {
//        if (event.isSneaking()) {
//            final Location base = event.getPlayer().getLocation().clone().add(0, 1, 0);
//
//            for (int i = 0; i < 5; i++) {
//                final ArmorStand armorStand = base.getWorld().spawn(base.add(0, 0.25, 0), ArmorStand.class);
//                armorStand.setVisible(false);
//                armorStand.setCustomNameVisible(true);
//                armorStand.setGravity(false);
//                armorStand.setCustomName("------------ LINE #" + i + " --------------");
//            }
//        }
//    }
}
