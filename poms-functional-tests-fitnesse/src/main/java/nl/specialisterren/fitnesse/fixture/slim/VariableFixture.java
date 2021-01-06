package nl.specialisterren.fitnesse.fixture.slim;

import java.util.prefs.Preferences;

public class VariableFixture {
	private Preferences prefs;
	

	public VariableFixture() {
		prefs = Preferences.userNodeForPackage(nl.specialisterren.fitnesse.fixture.slim.VariableFixture.class);
	}
	
	public String getVar(String variableName) {
		return prefs.get(variableName, "");
	}
	
	public void setVarTo(String variableName, String variableValue) {
		prefs.put(variableName, variableValue);
	}
}
