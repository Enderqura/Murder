package com.oasis.murder;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Enderqura on 18/07/2017 at 18:01.
 */
public class PlayerManager {

    static Map<Player, GameMechanics.Type> what = new HashMap<>();
    static int murdererKills = 0;

    public void setType(Player player, GameMechanics.Type type){

        what.put(player, type);

    }

    public void addMudererKill(){

        murdererKills++;

    }

}
