package jp.hutcraft.pt.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum Voice {
	Anm1("an-1", 0),
	Asm1("cn0", -2),
	Bnm1("cn0", -1),
	Cn0("cn0", 0),
	Cs0("ds0", -2),
	Dn0("ds0", -1),
	Ds0("ds0", 0),
	En0("fs0", -2),
	Fn0("fs0", -1),
	Fs0("fs0", 0),
	Gn0("an0", -2),
	Gs0("an0", -1),
	An0("an0", 0),
	As0("cn1", -2),
	Bn0("cn1", -1),
	Cn1("cn1", 0),
	Cs1("ds1", -2),
	Dn1("ds1", -1),
	Ds1("ds1", 0),
	En1("fs1", -2),
	Fn1("fs1", -1),
	Fs1("fs1", 0),
	Gn1("an1", -2),
	Gs1("an1", -1),
	An1("an1", 0),
	As1("cn2", -2),
	Bn1("cn2", -1),
	Cn2("cn2", 0),
	Cs2("ds2", -2),
	Dn2("ds2", -1),
	Ds2("ds2", 0),
	En2("fs2", -2),
	Fn2("fs2", -1),
	Fs2("fs2", 0),
	Gn2("an2", -2),
	Gs2("an2", -1),
	An2("an2", 0),
	As2("cn3", -2),
	Bn2("cn3", -1),
	Cn3("cn3", 0),
	Cs3("ds3", -2),
	Dn3("ds3", -1),
	Ds3("ds3", 0),
	En3("fs3", -2),
	Fn3("fs3", -1),
	Fs3("fs3", 0),
	Gn3("an3", -2),
	Gs3("an3", -1),
	An3("an3", 0),
	As3("cn4", -2),
	Bn3("cn4", -1),
	Cn4("cn4", 0),
	Cs4("ds4", -2),
	Dn4("ds4", -1),
	Ds4("ds4", 0),
	En4("fs4", -2),
	Fn4("fs4", -1),
	Fs4("fs4", 0),
	Gn4("an4", -2),
	Gs4("an4", -1),
	An4("an4", 0),
	As4("cn5", -2),
	Bn4("cn5", -1),
	Cn5("cn5", 0),
	Cs5("ds5", -2),
	Dn5("ds5", -1),
	Ds5("ds5", 0),
	En5("fs5", -2),
	Fn5("fs5", -1),
	Fs5("fs5", 0),
	Gn5("an5", -2),
	Gs5("an5", -1),
	An5("an5", 0),
	As5("cn6", -2),
	Bn5("cn6", -1),
	Cn6("cn6", 0),
	Cs6("ds6", -2),
	Dn6("ds6", -1),
	Ds6("ds6", 0),
	En6("fs6", -2),
	Fn6("fs6", -1),
	Fs6("fs6", 0),
	Gn6("an6", -2),
	Gs6("an6", -1),
	An6("an6", 0),
	As6("cn7", -2),
	Bn6("cn7", -1),
	Cn7("cn7", 0),
//	Cs7("ds7", -2),
//	Dn7("ds7", -1),
//	Ds7("ds7", 0),
//	En7("fs7", -2),
//	Fn7("fs7", -1),
//	Fs7("fs7", 0),
//	Gn7("an7", -2),
//	Gs7("an7", -1),
//	An7("an7", 0),
//	As7("cn8", -2),
//	Bn7("cn8", -1),
//	Cn8("cn8", 0),
	;
	private static final List<Voice> instances = new ArrayList<Voice>();
	static {
		instances.addAll(Arrays.asList(values()));
	}
	public static List<Voice> getInstances() {
		return Collections.unmodifiableList(instances);
	}
	private final String file;
	private final double adjust;
	private Voice(final String file, final double adjust) {
		this.file = String.format("resources/%s.wav", file);
		this.adjust = adjust;
	}
	public String getFile() {
		return file;
	}
	public double getAdjust() {
		return Math.pow(EqualTemperament.harfToneRate, adjust);
	}
	public boolean isBlack() {
		return name().charAt(1) == 's';
	}
	public String getLabel() {
		return String.format("%s%s%s",
				name().charAt(0),
				(name().charAt(1) == 's' ? "#" : ""),
				name().charAt(2) == 'm' ? ("-"+name().charAt(3)) : name().substring(2));
	}
}
