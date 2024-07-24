/* NFCard is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 3 of the License, or
(at your option) any later version.

NFCard is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Wget.  If not, see <http://www.gnu.org/licenses/>.

Additional permission under GNU GPL version 3 section 7 */

package com.example.ticket.nfc.card;

import android.nfc.tech.NfcF;

import com.example.ticket.nfc.Util;
import com.example.ticket.nfc.tech.FeliCa;

final class OctopusCard {
	private static final int SYS_SZT = 0x8005;
	private static final int SRV_SZT = 0x0118;
	private static final int SYS_OCTOPUS = 0x8008;
	private static final int SRV_OCTOPUS = 0x0117;

	static String load(NfcF tech) {
		final FeliCa.Tag tag = new FeliCa.Tag(tech);

		/*--------------------------------------------------------------*/
		// check card system
		/*--------------------------------------------------------------*/

		final int system = tag.getSystemCode();
		final FeliCa.ServiceCode service;
		if (system == SYS_OCTOPUS)
			service = new FeliCa.ServiceCode(SRV_OCTOPUS);
		else if (system == SYS_SZT)
			service = new FeliCa.ServiceCode(SRV_SZT);
		else
			return null;

		tag.connect();

		/*--------------------------------------------------------------*/
		// read service data without encryption
		/*--------------------------------------------------------------*/

		final float[] data = new float[] { 0, 0, 0 };
		final int N = data.length;

		int p = 0;
		for (byte i = 0; p < N; ++i) {
			final FeliCa.ReadResponse r = tag.readWithoutEncryption(service, i);
			if (!r.isOkey())
				break;

			data[p++] = (Util.toInt(r.getBlockData(), 0, 4) - 350) / 10.0f;
		}

		tag.close();

		/*--------------------------------------------------------------*/
		// build result string
		/*--------------------------------------------------------------*/

		final String name = parseName(system);
		final String info = parseInfo(tag);
		final String hist = parseLog(null);
		final String cash = parseBalance(data, p);

		return CardManager.buildResult(name, info, cash, hist);
	}

	private static String parseName(int system) {
		if (system == SYS_OCTOPUS)
			return "八达通卡（香港）";

		if (system == SYS_SZT)
			return "深圳通卡（旧版）";

		return null;
	}

	private static String parseInfo(FeliCa.Tag tag) {
		final StringBuilder r = new StringBuilder();
		final String i = "标识";
		final String p = "参数";
		r.append("<b>").append(i).append("</b> ")
				.append(tag.getIDm().toString());
		r.append("<br />").append(p).append(' ')
				.append(tag.getPMm().toString());

		return r.toString();
	}

	private static String parseBalance(float[] value, int count) {
		if (count < 1)
			return null;

		final StringBuilder r = new StringBuilder();
		final String s = "余额";
		final String c = "港币";

		r.append("<b>").append(s).append(" <font color=\"teal\">");

		for (int i = 0; i < count; ++i)
			r.append(Util.toAmountString(value[i])).append(' ');

		return r.append(c).append("</font></b>").toString();
	}

	private static String parseLog(byte[] data) {
		return null;
	}
}
