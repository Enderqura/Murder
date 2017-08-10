package com.oasis.murder;

import com.oasis.ranks.Rank;
import com.oasis.ranks.Ranks;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Enderqura on 16/07/2017 at 09:31.
 */
public class GameMechanics implements Listener, CommandExecutor{

    public final int playersNeeded = 5;
    public final int maxPlayers = 10;
    public final int lobbyCountdown = 30;
    public int countDownLeft = 30;
    public boolean counting = false;
    public int playersLeft = 10;
    public int innocentLeft = 8;
    private final Main plugin = Main.getPlugin(Main.class);
    private Location spawn = new Location(Bukkit.getWorld("world"), -27.0 ,98.0, -1803.0);
    public Player murderer, detective;
    private List<Player> forcestartconfirm = new ArrayList<>();
    public Ranks ranks = Ranks.getPlugin(Ranks.class);

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){

        if(Bukkit.getOnlinePlayers().size() > maxPlayers){

            //TODO: Bungee pushback
            return;

        }

        event.setJoinMessage("§e" + event.getPlayer().getName() + " has joined the game §7(§f" + Bukkit.getOnlinePlayers().size() + "/" + maxPlayers + "§7)");

        if(Bukkit.getOnlinePlayers().size() >= playersNeeded){

            if(!counting) {



                counting = true;
                gameStartCountdown();
            }

        }


    }

    public void gameStartCountdown(){

        new BukkitRunnable(){


            public void run(){

                countDownLeft--;
                if(Bukkit.getOnlinePlayers().size() < playersNeeded){

                    counting = false;
                    countDownLeft =+ 30;

                }

            }

        }.runTaskTimerAsynchronously(plugin, 20, lobbyCountdown*30);

        if(counting == false){

            Bukkit.broadcastMessage("Players left, game not starting.");
            return;

        }

        gameStart();


    }

    @EventHandler
    public void onHit(EntityDamageByEntityEvent e){

        if(e.getDamager() instanceof Player){

            Player player = (Player) e.getDamager();
            if(player.getItemInHand().getType() == Material.IRON_SWORD){

                if(e.getEntity() instanceof Player){

                    Player killed = (Player) e.getEntity();
                    Location l = killed.getLocation();

                    killed.setHealth(0.0D);

                    Bukkit.broadcastMessage(killed.getName() + " was stabbed by the murderer!");

                    if(killed.equals(detective)){

                        Bukkit.getWorld("world").dropItem(l, new ItemStack(Material.BOW));
                        Bukkit.broadcastMessage("The detective was killed! A bow has been dropped at their deathpoint!");

                    }else{

                        innocentLeft--;

                    }


                    playersLeft--;

                    if(playersLeft == 1){

                        win(Type.MURDERER);

                    }

                    return;

                }else{

                    e.setCancelled(true);
                    return;

                }

            }else if(player.getItemInHand().getType() == Material.BOW) {

                if(e.getEntity() instanceof Player){

                    Player killed = (Player) e.getEntity();

                    killed.setHealth(0.0D);

                    Bukkit.broadcastMessage(killed.getName() + " was shot by the detective!");


                    playersLeft--;

                    if(killed.equals(murderer)){

                        Bukkit.broadcastMessage("The murderer was killed!");
                        if(player.equals(detective)){

                            win(Type.DETECTIVE);

                        }else{

                            win(Type.INNOCENT);
                        }

                    }else{

                        Location l = player.getLocation();

                        player.setHealth(0.0D);

                        Bukkit.getWorld("world").dropItem(l, new ItemStack(Material.BOW));

                        Bukkit.broadcastMessage("The detective killed an innocent and was killed for their crime! A bow has been dropped at their location.");
                        innocentLeft--;
                        playersLeft--;

                    }



                    return;

                }else{

                    e.setCancelled(true);
                    return;

                }

            }else{

                e.setCancelled(true);
                return;

            }

        }else{

            e.setCancelled(true);
            return;

        }

    }

    public void gameStart(){


        playersLeft = Bukkit.getOnlinePlayers().size();
        innocentLeft = Bukkit.getOnlinePlayers().size() - 2;

        List<Player> list = new ArrayList<>();

        for(Player online : Bukkit.getOnlinePlayers()){

            list.add(online);

        }

        murderer = list.get(new Random().nextInt(playersLeft));
        detective = list.get(new Random().nextInt(playersLeft));

        while(murderer.equals(detective)){

            detective = list.get(new Random().nextInt(playersLeft));

        }

        murderer.sendMessage("You are the murderer, kill everyone without getting caught!");
        detective.sendMessage("You are the detective, find and kill the murderer!");

        List<Player> innocents = new ArrayList<>();

        for(Player online : Bukkit.getOnlinePlayers()){

            online.teleport(spawn);

            if(online.equals(murderer)){

                continue;
            }

            if(online.equals(detective)){


                continue;
            }

            innocents.add(online);
            online.sendMessage("You are innocent, stay alive as long as possible!");

        }

        giveItems();



    }

    public void giveItems(){

        new BukkitRunnable(){

            public void run(){

                detective.getInventory().clear();
                detective.getInventory().setItem(1, new ItemStack(Material.BOW));
                detective.getInventory().setItem(2, new ItemStack(Material.ARROW, 64));

                murderer.getInventory().clear();
                murderer.getInventory().setItem(1, new ItemStack(Material.IRON_SWORD));

            }

        }.runTaskLaterAsynchronously(plugin, 600L);

    }

    public void win(Type type){

        if(type == Type.DETECTIVE){

            Bukkit.broadcastMessage("The detective has won the game!");
            //Add coins

        }

        else if (type == Type.MURDERER){

            Bukkit.broadcastMessage("The murderer has won the game!");
            //Add coins

        }

        else{

            Bukkit.broadcastMessage("The innocents have won the game!");
            //TODO: Add coins


        }

        //TODO: Bungee Push Back

    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if(command.getName().equalsIgnoreCase("forcestart")){

            if(sender instanceof Player){

                Player player = (Player) sender;

                if(ranks.rankAtLeast(player, Rank.SYSTEMS_ADMINISTRATOR)){

                    if(forcestartconfirm.contains(player)){

                        player.sendMessage("Force-started the game!");
                        forcestartconfirm.remove(player);
                        gameStart();
                        return true;

                    }else{

                        player.sendMessage("This command may crash the server if no enough people are in the game! Use at your own risk. Re-run the command to confirm usage.");
                        forcestartconfirm.add(player);
                        return true;

                    }

                }else{

                    player.sendMessage("Not enough perms. You must be at least Systems Administrator!");
                    return true;

                }

            }else{

                sender.sendMessage("Force-started the game!");
                gameStart();
                return true;

            }

        }

        return false;
    }

    public enum Type{


        INNOCENT,
        MURDERER,
        DETECTIVE;

    }



}
