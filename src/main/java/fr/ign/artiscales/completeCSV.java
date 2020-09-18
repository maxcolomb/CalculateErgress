package fr.ign.artiscales;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;

import org.locationtech.jts.algorithm.MinimumBoundingCircle;
import org.locationtech.jts.algorithm.construct.MaximumInscribedCircle;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

public class completeCSV {

	public static void main(String[] args) throws IOException, ParseException {
		File root = new File("/home/mcolomb/workspace/ParcelManager/src/main/resources/ParcelComparison/out/");
		 completeCSVFile(new File(root, "SimulatedParcelStats.csv"));
		 completeCSVFile(new File(root, "EvolvedParcelStats.csv"));
		for (File f : root.listFiles())
			if (f.isDirectory())
				for (File ff : f.listFiles())
					if (ff.getName().endsWith(".csv"))
						completeCSVFile(ff);

	}

	public static File completeCSVFile(File inputFile) throws IOException, ParseException {

		File tmpFile = new File(inputFile.getParentFile(), inputFile.getName() + "tmp");
		CSVReader csv = new CSVReader(new FileReader(inputFile));
		String[] firstLine = csv.readNext();
		if (Arrays.asList(firstLine).contains("ergress")) {
			csv.close();
			return inputFile;
		}
		CSVWriter csvW = new CSVWriter(new FileWriter(tmpFile));

		int iGeom = 999;
		for (int i = 0; i < firstLine.length; i++) {
			String entete = firstLine[i];
			if (entete.equals("Geometry")) {
				iGeom = i;
				break;
			}
		}
		String[] newFirstLine = new String[firstLine.length + 1];
		for (int i = 0; i < firstLine.length; i++)
			newFirstLine[i] = firstLine[i];
		newFirstLine[firstLine.length] = "ergress";
		csvW.writeNext(newFirstLine);
		WKTReader reader = new WKTReader();
		for (String[] line : csv.readAll()) {
			try {
				String[] newLine = new String[line.length + 1];
				for (int i = 0; i < line.length; i++)
					newLine[i] = line[i];
				Geometry geom = reader.read(line[iGeom]);
				MinimumBoundingCircle mbc = new MinimumBoundingCircle(geom);
				MaximumInscribedCircle mic = new MaximumInscribedCircle(geom, 1);
				newLine[line.length] = String.valueOf(mic.getRadiusLine().getLength() / mbc.getDiameter().getLength());
				csvW.writeNext(newLine);
			} catch (ArrayIndexOutOfBoundsException ar) {
				System.out.println("that line be outta bound" + line[0]);
			}
		}
		csvW.close();
		csv.close();
		Files.delete(inputFile.toPath());
		Files.copy(tmpFile.toPath(), inputFile.toPath());
		Files.delete(tmpFile.toPath());
		return inputFile;
	}

}
