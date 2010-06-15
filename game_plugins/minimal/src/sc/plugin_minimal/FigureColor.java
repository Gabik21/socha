package sc.plugin_minimal;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Alle Spielfiguren aus dem Hase und Igel Original Mit Veränderungen der CAU
 */
@XStreamAlias(value="minimal:color")
public enum FigureColor
{
	/**
	 * Der erste Spieler ist immer rot 
	 */
	RED,
	/**
	 *  Der zweite Spieler ist immer blau
	 */
	BLUE
}