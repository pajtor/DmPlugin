package com.deeme.behaviours.bestability;

import java.util.Arrays;
import java.util.Collection;

import com.deeme.modules.temporal.AmbulanceModule;
import com.deeme.types.SharedFunctions;
import com.deeme.types.VerifierChecker;
import com.deeme.types.backpage.Utils;
import com.github.manolo8.darkbot.extensions.util.Version;

import eu.darkbot.api.PluginAPI;
import eu.darkbot.api.config.ConfigSetting;
import eu.darkbot.api.extensions.Behavior;
import eu.darkbot.api.extensions.Configurable;
import eu.darkbot.api.extensions.Feature;
import eu.darkbot.api.game.entities.Entity;
import eu.darkbot.api.game.entities.Npc;
import eu.darkbot.api.game.entities.Ship;
import eu.darkbot.api.game.group.GroupMember;
import eu.darkbot.api.game.items.ItemFlag;
import eu.darkbot.api.game.items.SelectableItem.Ability;
import eu.darkbot.api.game.other.Lockable;
import eu.darkbot.api.game.other.Movable;
import eu.darkbot.api.managers.AuthAPI;
import eu.darkbot.api.managers.BotAPI;
import eu.darkbot.api.managers.EntitiesAPI;
import eu.darkbot.api.managers.GroupAPI;
import eu.darkbot.api.managers.HeroAPI;
import eu.darkbot.api.managers.HeroItemsAPI;
import eu.darkbot.api.utils.Inject;
import eu.darkbot.shared.utils.SafetyFinder;
import eu.darkbot.shared.utils.SafetyFinder.Escaping;

@Feature(name = "Auto Best Ability", description = "Auto use the best ability. Can use almost all abilities")
public class AutoBestAbility implements Behavior, Configurable<BestAbilityConfig> {

    protected final PluginAPI api;
    protected final BotAPI bot;
    protected final HeroAPI heroapi;
    protected final GroupAPI group;
    protected final HeroItemsAPI items;
    protected final SafetyFinder safety;
    private BestAbilityConfig config;
    private Collection<? extends Ship> allShips;

    private long nextCheck = 0;

    public AutoBestAbility(PluginAPI api) {
        this(api, api.requireAPI(AuthAPI.class),
                api.requireAPI(BotAPI.class), api.requireAPI(HeroItemsAPI.class));
    }

    @Inject
    public AutoBestAbility(PluginAPI api, AuthAPI auth, BotAPI bot, HeroItemsAPI items) {
        if (!Arrays.equals(VerifierChecker.class.getSigners(), getClass().getSigners()))
            throw new SecurityException();
        VerifierChecker.checkAuthenticity(auth);

        Utils.showDonateDialog();

        this.api = api;
        this.bot = bot;
        this.items = items;
        this.heroapi = api.getAPI(HeroAPI.class);
        this.group = api.getAPI(GroupAPI.class);
        this.safety = api.requireInstance(SafetyFinder.class);

        EntitiesAPI entities = api.getAPI(EntitiesAPI.class);
        this.allShips = entities.getShips();
    }

    @Override
    public void setConfig(ConfigSetting<BestAbilityConfig> arg0) {
        this.config = arg0.getValue();
    }

    @Override
    public void onTickBehavior() {
        if (nextCheck < System.currentTimeMillis()) {
            nextCheck = System.currentTimeMillis() + 2000;
            Entity target = heroapi.getLocalTarget();
            if (target != null && target.isValid()) {
                if (config.npcEnabled || !(target instanceof Npc)) {
                    useSelectableReadyWhenReady(getBestAbility());
                }
            } else if (safety.state() == Escaping.ENEMY) {
                useSelectableReadyWhenReady(getBestAbility());
            }
        }
    }

    private Ability getBestAbility() {
        if (shoulFocusHealth()) {
            if (isAvailable(Ability.AEGIS_REPAIR_POD)) {
                return Ability.AEGIS_REPAIR_POD;
            } else if (isAvailable(Ability.AEGIS_HP_REPAIR)) {
                return Ability.AEGIS_HP_REPAIR;
            } else if (isAvailable(Ability.LIBERATOR_PLUS_SELF_REPAIR)) {
                return Ability.LIBERATOR_PLUS_SELF_REPAIR;
            } else if (isAvailable(Ability.SOLACE)) {
                return Ability.SOLACE;
            } else if (bot.getVersion().compareTo(new Version("1.13.17 beta 109 alpha 14")) > 1
                    && isAvailable(Ability.SOLACE_PLUS_NANO_CLUSTER_REPAIRER_PLUS)) {
                return Ability.SOLACE_PLUS_NANO_CLUSTER_REPAIRER_PLUS;
            }
        }
        if (shoulFocusShield()
                && isAvailable(Ability.AEGIS_SHIELD_REPAIR)) {
            return Ability.AEGIS_SHIELD_REPAIR;
        }
        if (shoulFocusSpeed()) {
            if (isAvailable(Ability.CITADEL_TRAVEL)) {
                return Ability.CITADEL_TRAVEL;
            } else if (isAvailable(Ability.LIGHTNING)) {
                return Ability.LIGHTNING;
            } else if (isAvailable(Ability.TARTARUS_SPEED_BOOST)) {
                return Ability.TARTARUS_SPEED_BOOST;
            } else if (isAvailable(Ability.KERES_SPR)) {
                return Ability.KERES_SPR;
            } else if (isAvailable(Ability.RETIARUS_SPC)) {
                return Ability.RETIARUS_SPC;
            } else if (isAvailable(Ability.MIMESIS_PHASE_OUT)) {
                return Ability.MIMESIS_PHASE_OUT;
            } else if (isAvailable(Ability.ZEPHYR_MMT)) {
                return Ability.ZEPHYR_MMT;
            }
        }
        if (shoulFocusEvade()) {
            if (isAvailable(Ability.SPEARHEAD_ULTIMATE_CLOAK)) {
                return Ability.SPEARHEAD_ULTIMATE_CLOAK;
            } else if (isAvailable(Ability.BERSERKER_RVG)) {
                return Ability.BERSERKER_RVG;
            } else if (isAvailable(Ability.MIMESIS_SCRAMBLE)) {
                return Ability.MIMESIS_SCRAMBLE;
            } else if (isAvailable(Ability.DISRUPTOR_DDOL)) {
                return Ability.DISRUPTOR_DDOL;
            }
        }
        if (shouldFocusHelpTank()) {
            if (isAvailable(Ability.CITADEL_DRAW_FIRE)) {
                return Ability.CITADEL_DRAW_FIRE;
            } else if (isAvailable(Ability.CITADEL_PROTECTION)) {
                return Ability.CITADEL_PROTECTION;
            }
        }
        if (shoulFocusEvade()) {
            if (isAvailable(Ability.CITADEL_PLUS_PRISMATIC_ENDURANCE)) {
                return Ability.CITADEL_PLUS_PRISMATIC_ENDURANCE;
            } else if (isAvailable(Ability.CITADEL_FORTIFY)) {
                return Ability.CITADEL_FORTIFY;
            } else if (isAvailable(Ability.DISRUPTOR_REDIRECT)) {
                return Ability.DISRUPTOR_REDIRECT;
            } else if (isAvailable(Ability.SPECTRUM)) {
                return Ability.SPECTRUM;
            } else if (isAvailable(Ability.SENTINEL)) {
                return Ability.SENTINEL;
            } else if (isAvailable(Ability.BERSERKER_BSK)) {
                return Ability.BERSERKER_BSK;
            } else if (isAvailable(Ability.ORCUS_ASSIMILATE)) {
                return Ability.ORCUS_ASSIMILATE;
            }
        }
        if (shoulFocusDamage()) {
            if (isAvailable(Ability.SPEARHEAD_TARGET_MARKER)) {
                return Ability.SPEARHEAD_TARGET_MARKER;
            } else if (isAvailable(Ability.DIMINISHER)) {
                return Ability.DIMINISHER;
            } else if (isAvailable(Ability.GOLIATH_X_FROZEN_CLAW)) {
                return Ability.GOLIATH_X_FROZEN_CLAW;
            } else if (isAvailable(Ability.VENOM)) {
                return Ability.VENOM;
            } else if (isAvailable(Ability.SOLARIS_INC)) {
                return Ability.SOLARIS_INC;
            } else if (isAvailable(Ability.TARTARUS_RAPID_FIRE)) {
                return Ability.TARTARUS_RAPID_FIRE;
            } else if (isAvailable(Ability.DISRUPTOR_SHIELD_DISARRAY)) {
                return Ability.DISRUPTOR_SHIELD_DISARRAY;
            } else if (isAvailable(Ability.HECATE_PARTICLE_BEAM)) {
                return Ability.HECATE_PARTICLE_BEAM;
            } else if (isAvailable(Ability.KERES_SPR)) {
                return Ability.KERES_SPR;
            } else if (isAvailable(Ability.ZEPHYR_TBR)) {
                return Ability.ZEPHYR_TBR;
            } else if (isAvailable(Ability.HOLO_ENEMY_REVERSAL)) {
                return Ability.HOLO_ENEMY_REVERSAL;
            }
        }

        return null;
    }

    private boolean shoulFocusDamage() {
        Lockable target = heroapi.getLocalTarget();
        if (target != null && target.isValid()) {
            if (target.getEntityInfo() != null && target.getEntityInfo().isEnemy()
                    && target.getHealth().hpPercent() > 0.3) {
                return true;
            }
        }

        return false;
    }

    private boolean shoulFocusSpeed() {
        if (safety.state() == Escaping.ENEMY) {
            return true;
        }
        Lockable target = heroapi.getLocalTarget();
        if (target != null && target.isValid()) {
            double distance = heroapi.getLocationInfo().getCurrent().distanceTo(target.getLocationInfo());
            double speed = target instanceof Movable ? ((Movable) target).getSpeed() : 0;
            return distance > 800 && speed > heroapi.getSpeed();
        }
        return false;
    }

    private boolean shoulFocusHealth() {
        if (bot.getModule() != null && bot.getModule().getClass() == AmbulanceModule.class) {
            return false;
        } else if (heroapi.getEffects() != null
                && heroapi.getEffects().toString().contains("76")) {
            return false;
        } else if (heroapi.getHealth().hpPercent() <= 0.5) {
            return true;
        } else if (group.hasGroup()) {
            for (GroupMember member : group.getMembers()) {
                if (!member.isDead() && member.isAttacked() && member.isLocked()
                        && member.getMemberInfo().hpPercent() <= 0.5) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean shouldFocusHelpTank() {
        if (heroapi.getHealth().shieldPercent() < 0.5) {
            return false;
        } else if (group.hasGroup()) {
            for (GroupMember member : group.getMembers()) {
                if (!member.isDead() && member.isAttacked() && member.getLocation().distanceTo(heroapi) < 1000) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean shoulFocusShield() {
        if (heroapi.getHealth().shieldPercent() < 0.5) {
            return true;
        } else if (group.hasGroup()) {
            for (GroupMember member : group.getMembers()) {
                if (!member.isDead() && member.isAttacked() && member.isLocked()
                        && member.getMemberInfo().shieldPercent() < 0.5) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean shoulFocusEvade() {
        if (allShips == null || allShips.isEmpty()) {
            return false;
        }
        Entity target = SharedFunctions.getAttacker(heroapi, allShips, heroapi);
        return target != null;
    }

    private boolean isAvailable(Ability ability) {
        return ability != null
                && items.getItem(ability, ItemFlag.USABLE, ItemFlag.READY, ItemFlag.AVAILABLE).isPresent();
    }

    private boolean useSelectableReadyWhenReady(Ability selectableItem) {
        return (selectableItem != null
                && items.useItem(selectableItem, 500, ItemFlag.USABLE, ItemFlag.READY).isSuccessful());
    }
}