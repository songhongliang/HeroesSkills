package com.herocraftonline.heroes.characters.skill.skills;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillType;
import com.herocraftonline.heroes.characters.skill.TargettedSkill;
import com.herocraftonline.heroes.util.Setting;

import me.Whatshiywl.heroesskilltree.HeroesSkillTree;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.configuration.ConfigurationSection;

public class SkillHeal extends TargettedSkill{
	
	public HeroesSkillTree hst = (HeroesSkillTree)Bukkit.getServer().getPluginManager().getPlugin("HeroesSkillTree");

    public SkillHeal(Heroes plugin) {
        super(plugin, "Heal");
        setDescription("Heals the target for $1");
        setUsage("/skill Heal");
        setArgumentRange(0, 0);
        setIdentifiers(new String[]{"skill Heal"});
        
        setTypes(SkillType.SILENCABLE, SkillType.HEAL, SkillType.LIGHT);
    }

    @Override
    public String getDescription(Hero hero) {
        String description = getDescription();
        
        //AMOUNT
        float amount = (float) (SkillConfigManager.getUseSetting(hero, this, Setting.AMOUNT.node(), 0.1, false) +
                (SkillConfigManager.getUseSetting(hero, this, "amount-increase", 0.0, false) * hero.getSkillLevel(this)));
        if(hst != null) amount += (SkillConfigManager.getUseSetting(hero, this, "hst-amount", 0.0, false) * (hst.getSkillLevel(hero, this) - 1));
        amount = amount > 0 ? amount : 0;
        if(amount > 0) {
        	description = getDescription().replace("$1", amount*100 + "%");
        }
        
        //COOLDOWN
        int cooldown = (SkillConfigManager.getUseSetting(hero, this, Setting.COOLDOWN.node(), 0, false)
                - SkillConfigManager.getUseSetting(hero, this, Setting.COOLDOWN_REDUCE.node(), 0, false) * hero.getSkillLevel(this)) / 1000;
        if (cooldown > 0) {
            description += " CD:" + cooldown + "s";
        }
        
        //MANA
        int mana = SkillConfigManager.getUseSetting(hero, this, Setting.MANA.node(), 10, false)
                - (SkillConfigManager.getUseSetting(hero, this, Setting.MANA_REDUCE.node(), 0, false) * hero.getSkillLevel(this));
        if (mana > 0) {
            description += " M:" + mana;
        }
        
        //HEALTH_COST
        int healthCost = SkillConfigManager.getUseSetting(hero, this, Setting.HEALTH_COST, 0, false) - 
                (SkillConfigManager.getUseSetting(hero, this, Setting.HEALTH_COST_REDUCE, mana, true) * hero.getSkillLevel(this));
        if (healthCost > 0) {
            description += " HP:" + healthCost;
        }
        
        //STAMINA
        int staminaCost = SkillConfigManager.getUseSetting(hero, this, Setting.STAMINA.node(), 0, false)
                - (SkillConfigManager.getUseSetting(hero, this, Setting.STAMINA_REDUCE.node(), 0, false) * hero.getSkillLevel(this));
        if (staminaCost > 0) {
            description += " FP:" + staminaCost;
        }
        
        //RADIUS
        int radius = SkillConfigManager.getUseSetting(hero, this, Setting.RADIUS.node(), 1, false) + 
        		(SkillConfigManager.getUseSetting(hero, this, Setting.RADIUS_INCREASE.node(), 0, false)) * hero.getSkillLevel(this);
        if(hst != null) radius += (SkillConfigManager.getUseSetting(hero, this, "hst-radius", 0.0, false) * (hst.getSkillLevel(hero, this) - 1));
        radius = radius > 1 ? radius : 1;
        description += " R:" + radius;
        
        //DELAY
        int delay = SkillConfigManager.getUseSetting(hero, this, Setting.DELAY.node(), 0, false) / 1000;
        if (delay > 0) {
            description += " W:" + delay + "s";
        }
        
        //EXP
        int exp = SkillConfigManager.getUseSetting(hero, this, Setting.EXP.node(), 0, false);
        if (exp > 0) {
            description += " XP:" + exp;
        }
        return description;
    }

    @Override
    public ConfigurationSection getDefaultConfig() {
        ConfigurationSection node = super.getDefaultConfig();
        node.set(Setting.AMOUNT.node(), 0.1);
        node.set("amount-increase", 0.0);
        node.set("hst-amount", 0);
        node.set(Setting.RADIUS.node(), 1);
        node.set(Setting.RADIUS_INCREASE.node(), 0);
        node.set("hst-radius", 0);
        return node;
    }

    @Override
    public SkillResult use(Hero hero, LivingEntity target,String[] args) {
        Player player = hero.getPlayer();
        if (target instanceof Player) {
	        Player tplayer = (Player) target;
	        Hero thero = plugin.getCharacterManager().getHero((Player) target);
	        
	        int amount = (int) (SkillConfigManager.getUseSetting(hero, this, Setting.HEALTH.node(), 0.1, false) +
	                (SkillConfigManager.getUseSetting(hero, this, Setting.HEALTH_INCREASE.node(), 0.0, false) * hero.getSkillLevel(this)));
	        if(hst != null) amount += (SkillConfigManager.getUseSetting(hero, this, "hst-amount", 0.0, false) * (hst.getSkillLevel(hero, this) - 1));
	        amount = amount > 0 ? amount : 0;
	        int radius = SkillConfigManager.getUseSetting(hero, this, Setting.RADIUS.node(), 1, false) + 
	        		(SkillConfigManager.getUseSetting(hero, this, Setting.RADIUS_INCREASE.node(), 0, false)) * hero.getSkillLevel(this);
	        if(hst != null) radius += (SkillConfigManager.getUseSetting(hero, this, "hst-radius", 0.0, false) * (hst.getSkillLevel(hero, this) - 1));
	        radius = radius > 1 ? radius : 1;
		    
	        for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
		        if (entity == target) {
				    if(amount > 0) {
				    	thero.setHealth(tplayer.getHealth()*amount);
				    	thero.syncHealth();
				       	tplayer.getLocation().getWorld().playEffect(player.getLocation(), Effect.SMOKE, 0);
				        broadcastExecuteText(hero, target);
				    }
		        }
		    }
	        return SkillResult.NORMAL;
        }
        else return SkillResult.INVALID_TARGET;
    }
}