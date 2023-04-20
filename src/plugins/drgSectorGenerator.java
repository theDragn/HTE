package plugins;

import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorGeneratorPlugin;

import world.drgZarmazd;

public class drgSectorGenerator implements SectorGeneratorPlugin
{
    @Override
    public void generate(SectorAPI sector)
    {
        (new drgZarmazd()).generate(sector);
    }
}
