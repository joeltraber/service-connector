/*
 *-----------------------------------------------------------------------------*
 *                            Copyright � 2010 by                              *
 *                    STABILIT Informatik AG, Switzerland                      *
 *                            ALL RIGHTS RESERVED                              *
 *                                                                             *
 * Valid license from STABILIT is required for possession, use or copying.     *
 * This software or any other copies thereof may not be provided or otherwise  *
 * made available to any other person. No title to and ownership of the        *
 * software is hereby transferred. The information in this software is subject *
 * to change without notice and should not be construed as a commitment by     *
 * STABILIT Informatik AG.                                                     *
 *                                                                             *
 * All referenced products are trademarks of their respective owners.          *
 *-----------------------------------------------------------------------------*
 */
/**
 * 
 */
package com.stabilit.sc.common.util;

import java.text.ParseException;
import java.util.Date;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.ValidationException;

import com.stabilit.sc.common.io.KeepAlive;
import com.stabilit.sc.common.io.SCMPHeaderType;

/**
 * @author JTraber
 * 
 */
public class ValidatorUtility {

	private static final String SCMP_VERSION_REGEX = "(\\d\\.\\d)-(\\d*)";
	private static final String IP_LIST_REGEX = "(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}/??)+";

	public static void validateSCMPVersion(String currenSCMPVersion,
			String incomingSCMPVersion) throws ValidationExcpetion {
		Pattern pattern = Pattern.compile(SCMP_VERSION_REGEX);

		Matcher matchCurr = pattern.matcher(currenSCMPVersion);
		matchCurr.find();

		float currReleaseAndVersionNr = Float.parseFloat(matchCurr.group(1));
		int currRevisionNr = Integer.parseInt(matchCurr.group(2));

		Matcher matchIn = pattern.matcher(incomingSCMPVersion);
		matchIn.find();

		float incReleaseAndVersionNr = Float.parseFloat(matchIn.group(1));
		int incRevisionNr = Integer.parseInt(matchIn.group(2));

		if (incReleaseAndVersionNr <= currReleaseAndVersionNr) {
			if (incReleaseAndVersionNr == currReleaseAndVersionNr
					&& incRevisionNr > currRevisionNr) {
				throw new ValidationExcpetion("SCMPVersion not compatible.");
			}
		} else {
			throw new ValidationExcpetion("SCMPVersion not compatible.");
		}
	}

	public static Date validateLocalDateTime(String localDateTimeString)
			throws ParseException {
		if (localDateTimeString == null) {
			return null;
		}
		// localDateTime validation with regex
		// Pattern pat = Pattern
		// .compile("[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}.[0-9]{3}\\+[0-9]{4}");
		// Matcher m = pat.matcher(localDateTimeString);
		// m.matches();
		Date localDateTime = DateTimeUtility.SDF.parse(localDateTimeString);
		return localDateTime;
	}

	public static Integer convertUnsignedInteger(Map<String, String> map,
			SCMPHeaderType key, Integer defaultValue) {

		String obj = map.get(key.getName());

		if (obj == null) {
			return defaultValue;
		}
		Integer value = Integer.parseInt(obj);
		return value;
	}

	public static KeepAlive validateKeepAlive(String keepAliveTimeout,
			String keepAliveInterval) throws ValidationException {
		int keepAliveTimeoutInt = Integer.parseInt(keepAliveTimeout);
		int keepAliveIntervalInt = Integer.parseInt(keepAliveInterval);

		if (keepAliveTimeoutInt > 3600 || keepAliveIntervalInt > 3600) {
			throw new ValidationException(
					"keepAliveTimeout or keepAliveInterval is to high.");
		}

		if (keepAliveTimeoutInt > 3600) {
			throw new ValidationException("keepAliveTimeout is to high.");
		}

		if ((keepAliveTimeoutInt == 0 && keepAliveIntervalInt != 0)
				|| (keepAliveIntervalInt == 0 && keepAliveTimeoutInt != 0)) {
			throw new ValidationException(
					"keepAliveTimeout and keepAliveInterval must either be both zero or both non zero!");
		}
		return new KeepAlive(keepAliveTimeoutInt, keepAliveIntervalInt);
	}

	public static void validateIpAddressList(String ipAddressListString)
			throws ValidationException {
		
		Pattern pat = Pattern.compile(IP_LIST_REGEX);
		Matcher m = pat.matcher(ipAddressListString);
		if (!m.matches()) {
			throw new ValidationException("iplist has wrong format.");
		}
	}

	public static String validateInt(int lowerLimit, String intStringValue)
			throws ValidationException {
		if (intStringValue == null) {
			throw new ValidationException("intValue must be set.");
		}
		int intValue = 0;
		try {
			intValue = Integer.parseInt(intStringValue);
		} catch (NumberFormatException ex) {
			throw new ValidationException("intValue must be numeric.");
		}

		if (intValue <= lowerLimit)
			throw new ValidationException("intValue to low.");

		return intStringValue;
	}

	public static String validateInt(int lowerLimit, String intStringValue,
			int upperLimit) throws ValidationException {
		if (intStringValue == null) {
			throw new ValidationException("intValue must be set.");
		}
		int intValue = 0;
		try {
			intValue = Integer.parseInt(intStringValue);
		} catch (NumberFormatException ex) {
			throw new ValidationException("intValue must be numeric.");
		}

		if (intValue <= lowerLimit || intValue >= upperLimit)
			throw new ValidationException("intValue not within limits.");

		return intStringValue;
	}
	
	public static void validateString(int minSize, String stringValue,
			int maxSize) throws ValidationException {
		int length = stringValue.getBytes().length;
		
		if(length < minSize || length > maxSize) {
			throw new ValidationException("stringValue length is not within limits.");
		}
	}
}
