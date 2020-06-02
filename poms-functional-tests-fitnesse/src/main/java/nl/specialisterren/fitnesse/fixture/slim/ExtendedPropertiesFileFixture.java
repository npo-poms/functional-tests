package nl.specialisterren.fitnesse.fixture.slim;

import nl.hsac.fitnesse.fixture.slim.PropertiesFileFixture;

public class ExtendedPropertiesFileFixture extends PropertiesFileFixture {
    public boolean loadValuesFrom(String filename) {
		try {
			return super.loadValuesFrom(filename);
		} catch (Exception e) {
			return false;
		}
	}
}
