package com.birlasoft.framework;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import com.birlasoft.utils.ExcelUtils;
import com.birlasoft.utils.FileHandlingUtils;

public class TestReport {
	private static TestReport report = new TestReport();
	static Logger LOGGER = Logger.getLogger(TestReport.class);

	public String dtTodaysDate, reportPath, summaryReport, detailedReport, screenshotPath;

	public String startTime = "", endTime = "", duration = "";

	private TestReport() {
	}

	public static TestReport getInstance() {
		return report;
	}

	public void reportingSetup(String configPath) throws IOException {
		report.reportPath = new File(
				String.valueOf(ExcelUtils.getCellValue(configPath, "Config", "Value", "Key=ReportsPath")).trim())
						.getCanonicalPath();
		report.screenshotPath = new File(
				String.valueOf(ExcelUtils.getCellValue(configPath, "Config", "Value", "Key=ScreenshotPath")).trim())
						.getCanonicalPath();
		String archivePath = new File(
				String.valueOf(ExcelUtils.getCellValue(configPath, "Config", "Value", "Key=ArchivePath")).trim())
						.getCanonicalPath();
		archiveResults(archivePath);
		report.initReport();
	}

	private void initReport() throws IOException {
		dtTodaysDate = DateFormat.getDateTimeInstance().format(new Date()).toString().replaceAll(":", "_")
				.replaceAll("\\s+", "_").replaceAll(",", "");
		summaryReport = reportPath + File.separatorChar + "Result_" + dtTodaysDate + ".html";
		detailedReport = reportPath + File.separatorChar + "DetailResult_" + dtTodaysDate + ".html";

		// Create Summary Report
		String strTCHtml = "<style>table.tableizer-table {border: 1px solid #CCC; font-family: Arial;}"
				+ ".tableizer-table td {padding: 4px; margin: 3px; border: 1px solid #ccc;}"
				+ ".tableizer-table th {background-color: #0000A0; color: #ffffff; font-weight: bold;}</style>"
				+ "<table class=\"tableizer-table\"><tr class=\"tableizer-firstrow\">"
				+ "<th>Suite Name</th><th>TCID</th><th>TCID</th><th>TestCaseName</th>"
				+ "<th>StartTime</th><th>EndTime</th><th>Status</th><th>Requirements</th>";

		BufferedWriter out = new BufferedWriter(new FileWriter(summaryReport, true));
		out.write(strTCHtml);
		out.close();

		FileHandlingUtils.createFile(summaryReport);

		// Create Detailed Report
		String strTSHtml = "<style>table.tableizer-table {border: 1px solid #CCC; font-family: Arial, Helvetica, sans-serif;"
				+ "font-size: 9px;} .tableizer-table td {padding: 4px; margin: 3px; border: 1px solid #ccc;}"
				+ ".tableizer-table th {background-color: #620B38; color: #FFF; font-weight: bold;}</style>"
				+ "<table class=\"tableizer-table\"><tr class=\"tableizer-firstrow\">"
				+ "<th>Suite Name</th><th>TCID</th><th>TCID</th><th>TestCaseName</th>"
				+ "<th>TestStepId</th><th>Action</th><th>ScreenName</th><th>ObjectName</th>"
				+ "<th>Fieldvalue</th><th>ExpectedResult</th><th>ActualResult</th>"
				+ "<th>ExecutionStatus</th><th>ScreenShot</th><th>StartTime</th><th>EndTime</th>";

		BufferedWriter outDetail = new BufferedWriter(new FileWriter(detailedReport, true));
		outDetail.write(strTSHtml);
		outDetail.close();

		FileHandlingUtils.createFile(detailedReport);
	}

	private void archiveResults(String archivePath) throws IOException {
		String todaysDate = DateFormat.getDateTimeInstance().format(new Date()).toString().replaceAll(":", "_")
				.replaceAll("\\s+", "_").replaceAll(",", "");
		String userName = System.getProperty("user.name");
		String hostName = "Global";

		try {
			hostName = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			LOGGER.error("Unknown Host Name", e);
		}

		String reportZip = archivePath + File.separatorChar + hostName + "_" + userName + "_Report_" + todaysDate
				+ ".zip";

		FileHandlingUtils.createDirectory(archivePath);
		FileHandlingUtils.zipDir(reportPath, reportZip);
		FileHandlingUtils.deleteDirectory(reportPath);
		FileHandlingUtils.createDirectory(reportPath);
		FileHandlingUtils.createDirectory(screenshotPath);
	}

	public void loginReport(String reportType, String data) throws IOException {
		FileWriter fWriter = null;

		if (reportType.toLowerCase().equals("summary")) {
			fWriter = new FileWriter(summaryReport, true);
		} else if (reportType.toLowerCase().equals("detail")) {
			fWriter = new FileWriter(detailedReport, true);
		}

		BufferedWriter outResult = null;

		synchronized (fWriter) {
			try {
				outResult = new BufferedWriter(fWriter);
				outResult.newLine();
				outResult.write(data);
			} finally {
				outResult.close();
			}
		}
	}

	public void createEmailableReport(int totalTCs, int totalPassTCs, int totalFailTCs) throws IOException {
		InetAddress localMachine = InetAddress.getLocalHost();
		String emailReportName = summaryReport.substring(0, summaryReport.indexOf(".html") - 1) + "_EMAILABLE.html";
		FileUtils.copyFile(new File(summaryReport), new File(emailReportName));
		BufferedWriter out = new BufferedWriter(new FileWriter(emailReportName));

		String detailReportLink = "";

		/*
		 * if (TestDriver.getInstance().getCIExecution()) { detailReportLink =
		 * "<a href=\"@build_path@/DetailResult_" + dtTodaysDate + ".html"; }
		 * else { detailReportLink =
		 * "<a href=\"file://///\\" + localMachine.getHostName() + "
		 * \\Test_Reports\\DetailResult_" + dtTodaysDate + ".html"; }
		 */
		detailReportLink = "<a href=\"file://///\\" + localMachine.getHostName() + "\\Test_Reports\\DetailResult_"
				+ dtTodaysDate + ".html";

		String strTestExecParam = "<style>table.tableizer-table {border: 1px solid #CCC; font-family: Arial;}"
				+ ".tableizer-table td {padding: 4px; margin: 3px; border: 1px solid #ccc;}"
				+ "{background-color: #0000A0; color: #ffffff; font-weight: bold;}</style>"
				+ "<h1 align=center>Automation Execution Report</h1>"
				+ "<hr /><h2 align=center>Environment Details</h1>";

		String strConfig = "<table align=center class=\"tableizer-table\">"
				+ "<tr class=\"tableizer-firstrow\"><th>Parameter Name</th><th>Parameter Value</th></tr>"
				+ "<tr><td>Test Environment</td><td>SIT</td></tr>" + "<tr><td>Build Version</td><td>1.9</td></tr>"
				+ "<tr><td>Execution Date</td><td>"
				+ new SimpleDateFormat("dd/MM/yyyy").format(Calendar.getInstance().getTime()) + "</td></tr>"
				+ "<tr><td>Detail Report</td><td>" + detailReportLink
				+ "\"target=\" _blank \"type=\" html\">Click To Open</a></td></tr>" + "<tr><td>Start Time</td><td>"
				+ startTime + "</td></tr>" + "<tr><td>End Time</td><td>" + endTime + "</td></tr>"
				+ "<tr><td>Duration</td><td>" + duration + "</td></tr></table><br><hr/>";

		String strReport = "<h2 align=center>Test Report</h2><table align=center class=\"tableizer-table\">"
				+ "<tr class=\"tableizer-firstrow\"><TR bgcolor= #0000a0>"
				+ "<TD align=middle><FONT color=#ffffff face=Arial>Test Summary - Total Run (" + totalTCs
				+ "), Passed (" + totalPassTCs + "), Failed (" + totalFailTCs + ").</FONT></TD></tr></table><br>";
		
		out.write(strTestExecParam);
		out.write(strConfig);
		out.write(strReport);
		
		// Append HTML from Summary report to the emailable report
		BufferedReader in = new BufferedReader(new FileReader(summaryReport));
		String strCurrentLine;
		String finalFile = "";
		
		while ((strCurrentLine = in.readLine()) != null) {
			finalFile += strCurrentLine;
		}
		
		in.close();
		out.append(finalFile);
		out.close();
	}
}