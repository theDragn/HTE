package commands;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.StarSystemAPI;

import org.lazywizard.console.BaseCommand;
import org.lazywizard.console.Console;

import world.drgZarmazd;

public class generateHTE implements BaseCommand
{
    @Override
    public CommandResult runCommand(String args, CommandContext context)
    {
        if (context == CommandContext.COMBAT_MISSION || context == CommandContext.COMBAT_SIMULATION || context == CommandContext.COMBAT_CAMPAIGN)
            return CommandResult.WRONG_CONTEXT;
        
        boolean genZarmazd = true;
        for (StarSystemAPI system : Global.getSector().getStarSystems())
        {
            if (system.getId().equals("zarmazd"))
            {
                Console.showMessage("Zarmazd already generated.");
                genZarmazd = false;
            }
        }

        if (genZarmazd)
        {
            new drgZarmazd().generate(Global.getSector());
            Console.showMessage("Generated Zarmazd system.");
        }
        return CommandResult.SUCCESS;
    }    
}
