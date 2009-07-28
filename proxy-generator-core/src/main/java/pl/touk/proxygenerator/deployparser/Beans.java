/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pl.touk.proxygenerator.deployparser;

/**
 *
 * @author azl
 */
public class Beans {

	private String http;
	private String atm;
	private String dh;
	private String qmm;
	private String mwk;
	private String ws;

	public Beans()
	{
	}

	public Beans(String http, String atm, String dh, String qmm, String mwk, String ws)
	{
		super();

		this.http = http;
		this.atm = atm;
		this.dh = dh;
		this.qmm = qmm;
		this.mwk = mwk;
		this.ws = ws;
	}

	public String getHttp() {
		return http;
	}

	public void setHttp(String http) {
		this.http = http;
	}

	public String getAtm() {
		return atm;
	}

	public void setAtm(String atm) {
		this.atm = atm;
	}

	public String getDh() {
		return dh;
	}

	public void setDh(String dh) {
		this.dh = dh;
	}

	public String getQmm() {
		return qmm;
	}

	public void setQmm(String qmm) {
		this.qmm = qmm;
	}

	public String getMwk() {
		return mwk;
	}

	public void setMwk(String mwk) {
		this.mwk = mwk;
	}

	public String getWs() {
		return ws;
	}

	public void setWs(String ws) {
		this.ws = ws;
	}

	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		sb.append(" { Beans Atributes --");
		sb.append("http:" + getHttp());
		sb.append(", ");
		sb.append("atm:" + getAtm());
		sb.append(", ");
		sb.append("dh:" + getDh());
		sb.append(", ");
		sb.append("qmm:" + getQmm());
		sb.append(", ");
		sb.append("mwk:" + getMwk());
		sb.append(", ");
		sb.append("ws:" + getWs());
		sb.append(". } \n ");

		return sb.toString();
	}
}
