package sc.plugin2010.framework;

import sc.plugin2010.util.GameUtil;

/**
 * Eine Werkzeug Klasse, um nützliche und häufig gebrauchte Funktionen zur
 * Verfügung zu stellen
 */
public class Werkzeuge
{
	/**
	 * berechnet die benötigte Karottenanzahl, die man haben muss, um
	 * <code>zugAnzahl</code> Fehler weiterzuziehen
	 * 
	 * @param zugAnzahl
	 *            die Anzahl an Feldern, die man nach vorne möchte
	 * @return Karottenanzahl, welche für diesen Zug benötigt wird
	 */
	public static int berechneBenoetigteKarotten(int zugAnzahl)
	{
		return GameUtil.calculateCarrots(zugAnzahl);
	}

	/**
	 * berechnet die maximale Zugzahl, welche man mit <code>karotten</code>
	 * machen kann
	 * 
	 * @param karrotten
	 *            die Anzahl an Karotten, die man ausgeben will
	 * @return Felderanzahl, welche man mit der Karottenanzahl ziehen kann
	 */
	public static int berechneMaximaleZugzahl(int karotten)
	{
		return GameUtil.calculateMoveableFields(karotten);
	}
}
