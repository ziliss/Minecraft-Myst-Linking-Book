package net.minecraft.src.mystlinkingbook;

import java.util.Scanner;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

/**
 * Represents and stores the informations about an Age area in a dimension.<br>
 * <br>
 * This class should only be instantiated by {@code LinkingBookDimensionAge}s.<br>
 * When an Age area is modified, don't forget to call the method {@link AgesManager#updatedAgeArea}
 * 
 * @author ziliss
 * @since 0.5a
 */
public class AgeArea implements Cloneable {
	
	public int dimension;
	
	public int id;
	
	public String name;
	
	public boolean disabled = false;
	
	public int pos1X;
	public int pos1Y;
	public int pos1Z;
	public boolean pos1Set = false;
	
	public int pos2X;
	public int pos2Y;
	public int pos2Z;
	public boolean pos2Set = false;
	
	/**
	 * Used to parse a position.<br>
	 * A position is composed of 3 ints separated by spaces.
	 * 
	 * @see java.util.regex.Pattern
	 */
	public static final Pattern posPattern = Pattern.compile("(-?\\d+)\\s+(-?\\d+)\\s+(-?\\d+)", Pattern.CASE_INSENSITIVE);
	
	public AgeArea(int dimension, int id) {
		this.dimension = dimension;
		this.id = id;
		this.name = "Age area " + id;
	}
	
	public AgeArea(int dimension, int id, String name, int pos1X, int pos1Y, int pos1Z, int pos2X, int pos2Y, int pos2Z) {
		this.dimension = dimension;
		this.id = id;
		this.name = name;
		this.pos1X = pos1X;
		this.pos1Y = pos1Y;
		this.pos1Z = pos1Z;
		pos1Set = true;
		this.pos2X = pos2X;
		this.pos2Y = pos2Y;
		this.pos2Z = pos2Z;
		pos2Set = true;
	}
	
	public boolean isInAge(int x, int y, int z) {
		//@formatter:off
		return isValid()
			&& x >= Math.min(pos1X, pos2X)
			&& x <= Math.max(pos1X, pos2X)
			&& y >= Math.min(pos1Y, pos2Y)
			&& y <= Math.max(pos1Y, pos2Y)
			&& z >= Math.min(pos1Z, pos2Z)
			&& z <= Math.max(pos1Z, pos2Z);
		//@formatter:on
	}
	
	public boolean intersects(AgeArea o) {
		//@formatter:off
		return isValid() && o.isValid()
			&& Math.max(o.pos1X, o.pos2X) >= Math.min(pos1X, pos2X)
			&& Math.min(o.pos1X, o.pos2X) <= Math.max(pos1X, pos2X)
			&& Math.max(o.pos1Y, o.pos2Y) >= Math.min(pos1Y, pos2Y)
			&& Math.min(o.pos1Y, o.pos2Y) <= Math.max(pos1Y, pos2Y)
			&& Math.max(o.pos1Z, o.pos2Z) >= Math.min(pos1Z, pos2Z)
			&& Math.min(o.pos1Z, o.pos2Z) <= Math.max(pos1Z, pos2Z);
		//@formatter:on
	}
	
	public boolean isValid() {
		return pos1Set && pos2Set;
	}
	
	public boolean setPos1(String value) {
		Scanner sc = new Scanner(value);
		if (sc.findInLine(posPattern) != null) {
			MatchResult result = sc.match();
			pos1X = Integer.parseInt(result.group(1));
			pos1Y = Integer.parseInt(result.group(2));
			pos1Z = Integer.parseInt(result.group(3));
			pos1Set = true;
			return true;
		}
		else {
			pos1Set = false;
			return false;
		}
	}
	
	public boolean setPos2(String value) {
		Scanner sc = new Scanner(value);
		if (sc.findInLine(posPattern) != null) {
			MatchResult result = sc.match();
			pos2X = Integer.parseInt(result.group(1));
			pos2Y = Integer.parseInt(result.group(2));
			pos2Z = Integer.parseInt(result.group(3));
			pos2Set = true;
			return true;
		}
		else {
			pos2Set = false;
			return false;
		}
	}
	
	public String getPos1() {
		return pos1Set ? pos1X + " " + pos1Y + " " + pos1Z : "";
	}
	
	public String getPos2() {
		return pos2Set ? pos2X + " " + pos2Y + " " + pos2Z : "";
	}
	
	public void setPos1(int x, int y, int z) {
		pos1X = x;
		pos1Y = y;
		pos1Z = z;
		pos1Set = true;
	}
	
	public void setPos2(int x, int y, int z) {
		pos2X = x;
		pos2Y = y;
		pos2Z = z;
		pos2Set = true;
	}
	
	public void unsetPos1() {
		pos1Set = false;
	}
	
	public void unsetPos2() {
		pos2Set = false;
	}
	
	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}
	
	public boolean same(AgeArea other) {
		return this.dimension == other.dimension && this.id == other.id;
	}
	
	public boolean sameData(AgeArea other) {
		boolean samePos1;
		if (pos1Set == other.pos1Set) {
			samePos1 = pos1Set ? pos1X == other.pos1X && pos1Y == other.pos1Y && pos1Z == other.pos1Z : true;
		}
		else {
			samePos1 = false;
		}
		boolean samePos2;
		if (pos2Set == other.pos2Set) {
			samePos2 = pos2Set ? pos2X == other.pos2X && pos2Y == other.pos2Y && pos2Z == other.pos2Z : true;
		}
		else {
			samePos2 = false;
		}
		return name.equals(other.name) && samePos1 && samePos2 && disabled == other.disabled;
	}
	
	public void copyDatasTo(AgeArea other) {
		other.name = this.name;
		other.pos1X = this.pos1X;
		other.pos1Y = this.pos1Y;
		other.pos1Z = this.pos1Z;
		other.pos1Set = this.pos1Set;
		other.pos2X = this.pos2X;
		other.pos2Y = this.pos2Y;
		other.pos2Z = this.pos2Z;
		other.pos2Set = this.pos2Set;
		other.disabled = this.disabled;
	}
	
	@Override
	public AgeArea clone() {
		try {
			return (AgeArea)super.clone();
		}
		catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder("AgeArea [dim=");
		builder.append(dimension).append(",id=").append(id).append(",name=\"").append(name);
		builder.append("\",pos1=").append(getPos1()).append(",pos2=").append(getPos2());
		if (disabled) {
			builder.append(",disabled");
		}
		return builder.append("]").toString();
	}
}
