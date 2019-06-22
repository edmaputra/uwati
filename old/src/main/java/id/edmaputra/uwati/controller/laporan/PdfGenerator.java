package id.edmaputra.uwati.controller.laporan;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Font.FontFamily;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import id.edmaputra.uwati.entity.transaksi.Pembelian;
import id.edmaputra.uwati.entity.transaksi.PembelianDetail;
import id.edmaputra.uwati.entity.transaksi.Penjualan;
import id.edmaputra.uwati.entity.transaksi.PenjualanDetail;
import id.edmaputra.uwati.support.Converter;

public class PdfGenerator {

	public ByteArrayInputStream laporanPenjualan(List<Penjualan> penjualans, String tanggalAwal, String tanggalAkhir,
			String tipe) {
		Document document = new Document(PageSize.A4.rotate());
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			PdfWriter.getInstance(document, out);
			document.open();

			Font headFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
			Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 24);

			Paragraph title = new Paragraph("Laporan Penjualan", titleFont);
			title.setSpacingAfter(5f);
			document.add(title);

			document.add(subheader(tanggalAwal + " s/d " + tanggalAkhir, tipe));

			BigDecimal sumGrandTotalPembelian = BigDecimal.ZERO;
			BigDecimal sumGrandPajak = BigDecimal.ZERO;
			BigDecimal sumGrandDiskon = BigDecimal.ZERO;

			for (Penjualan p : penjualans) {
				document.add(kolomPenjualanDetail("Tanggal Transaksi: " + Converter.dateToString(p.getWaktuTransaksi()),
						"Nomor Faktur: " + p.getNomorFaktur(), ""));
				PdfPTable detail = kolomPenjualanDetail("Dokter: " + p.getDokter(), "Pasien: " + p.getPelanggan(),
						"Kasir: " + p.getPengguna());
				detail.setSpacingAfter(3f);
				document.add(detail);

				PdfPTable table = new PdfPTable(5);
				table.setWidthPercentage(100);
				table.setWidths(new int[] { 3, 1, 1, 1, 1 });

				table.addCell(buatHeaderTableCell("Obat", headFont));
				table.addCell(buatHeaderTableCell("Harga Jual", headFont));
				table.addCell(buatHeaderTableCell("Diskon", headFont));
				table.addCell(buatHeaderTableCell("Jumlah", headFont));
				table.addCell(buatHeaderTableCell("Total", headFont));

				for (PenjualanDetail d : p.getPenjualanDetail()) {
					table.addCell(buatIsiTableCell(d.getObat(), null, Element.ALIGN_LEFT));
					table.addCell(
							buatIsiTableCell(Converter.patternCurrency(d.getHargaJual()), null, Element.ALIGN_CENTER));
					table.addCell(
							buatIsiTableCell(Converter.patternCurrency(d.getDiskon()), null, Element.ALIGN_CENTER));
					table.addCell(buatIsiTableCell(d.getJumlah().toString(), null, Element.ALIGN_CENTER));
					table.addCell(
							buatIsiTableCell(Converter.patternCurrency(d.getHargaTotal()), null, Element.ALIGN_CENTER));
				}

				table.setSpacingAfter(1f);
				document.add(table);

				PdfPTable tabelRincianBayar = new PdfPTable(6);
				tabelRincianBayar.setWidthPercentage(100);
				tabelRincianBayar.setWidths(new int[] { 1, 1, 1, 1, 1, 1 });

				tabelRincianBayar.addCell(buatIsiTableCell("Total Pembelian", headFont, Element.ALIGN_CENTER));
				tabelRincianBayar.addCell(buatIsiTableCell("Diskon", headFont, Element.ALIGN_CENTER));
				tabelRincianBayar.addCell(buatIsiTableCell("Pajak", headFont, Element.ALIGN_CENTER));
				tabelRincianBayar.addCell(buatIsiTableCell("Total Akhir", headFont, Element.ALIGN_CENTER));
				tabelRincianBayar.addCell(buatIsiTableCell("Pembayaran", headFont, Element.ALIGN_CENTER));
				tabelRincianBayar.addCell(buatIsiTableCell("Kembali", headFont, Element.ALIGN_CENTER));

				BigDecimal grandTotal = p.getGrandTotal();
				BigDecimal grandPajak = p.getPajak();
				BigDecimal grandDiskon = p.getDiskon();

				tabelRincianBayar.addCell(
						buatIsiTableCell(Converter.patternCurrency(p.getTotalPembelian()), null, Element.ALIGN_CENTER));
				tabelRincianBayar
						.addCell(buatIsiTableCell(Converter.patternCurrency(grandDiskon), null, Element.ALIGN_CENTER));
				tabelRincianBayar
						.addCell(buatIsiTableCell(Converter.patternCurrency(grandPajak), null, Element.ALIGN_CENTER));
				tabelRincianBayar
						.addCell(buatIsiTableCell(Converter.patternCurrency(grandTotal), null, Element.ALIGN_CENTER));
				tabelRincianBayar
						.addCell(buatIsiTableCell(Converter.patternCurrency(p.getBayar()), null, Element.ALIGN_CENTER));
				tabelRincianBayar.addCell(
						buatIsiTableCell(Converter.patternCurrency(p.getKembali()), null, Element.ALIGN_CENTER));

				sumGrandDiskon = sumGrandDiskon.add(grandDiskon);
				sumGrandPajak = sumGrandPajak.add(grandPajak);
				sumGrandTotalPembelian = sumGrandTotalPembelian.add(grandTotal);

				tabelRincianBayar.setSpacingAfter(23f);
				document.add(tabelRincianBayar);
			}

			PdfPTable tabelSum = new PdfPTable(3);
			tabelSum.setWidthPercentage(100);
			tabelSum.setWidths(new int[] { 1, 1, 1 });
			tabelSum.addCell(buatIsiTableCell("TOTAL PAJAK", headFont, Element.ALIGN_CENTER));
			tabelSum.addCell(buatIsiTableCell("TOTAL DISKON", headFont, Element.ALIGN_CENTER));
			tabelSum.addCell(buatIsiTableCell("TOTAL PENJUALAN", headFont, Element.ALIGN_CENTER));
			tabelSum.addCell(buatIsiTableCell(Converter.patternCurrency(sumGrandPajak), null, Element.ALIGN_CENTER));
			tabelSum.addCell(buatIsiTableCell(Converter.patternCurrency(sumGrandDiskon), null, Element.ALIGN_CENTER));
			tabelSum.addCell(
					buatIsiTableCell(Converter.patternCurrency(sumGrandTotalPembelian), null, Element.ALIGN_CENTER));
			tabelSum.setSpacingAfter(3f);
			document.add(tabelSum);
			document.close();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return new ByteArrayInputStream(out.toByteArray());
	}

	public ByteArrayInputStream laporanPembelian(List<Pembelian> pembelians, String tanggalAwal, String tanggalAkhir) {
		Document document = new Document(PageSize.A4.rotate());
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			PdfWriter.getInstance(document, out);
			document.open();

			Font headFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
			Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 24);

			Paragraph title = new Paragraph("LAPORAN PEMBELIAN", titleFont);
			title.setSpacingAfter(5f);
			document.add(title);

			document.add(subheader(tanggalAwal + " s/d " + tanggalAkhir));

			BigDecimal sumGrandTotal = BigDecimal.ZERO;
			BigDecimal sumGrandPajak = BigDecimal.ZERO;
			BigDecimal sumGrandDiskon = BigDecimal.ZERO;

			for (Pembelian p : pembelians) {				
				PdfPTable detail = kolomPenjualanDetail("Tanggal Transaksi: " + Converter.dateToString(p.getWaktuTransaksi()),
						"Nomor Faktur: " + p.getNomorFaktur(), "Supplier: "+p.getSupplier());
				detail.setSpacingAfter(3f);
				document.add(detail);

				PdfPTable table = new PdfPTable(6);
				table.setWidthPercentage(100);
				table.setWidths(new int[] { 3, 1, 1, 1, 1, 1 });

				table.addCell(buatHeaderTableCell("Obat", headFont));
				table.addCell(buatHeaderTableCell("Harga Beli", headFont));
				table.addCell(buatHeaderTableCell("Diskon", headFont));
				table.addCell(buatHeaderTableCell("Jumlah", headFont));
				table.addCell(buatHeaderTableCell("Total", headFont));
				table.addCell(buatHeaderTableCell("Harga Jual", headFont));

				for (PembelianDetail d : p.getPembelianDetail()) {
					table.addCell(buatIsiTableCell(d.getObat(), null, Element.ALIGN_LEFT));
					table.addCell(buatIsiTableCell(Converter.patternCurrency(d.getHargaBeli()), null, Element.ALIGN_CENTER));
					table.addCell(buatIsiTableCell(Converter.patternCurrency(d.getDiskon()), null, Element.ALIGN_CENTER));
					table.addCell(buatIsiTableCell(d.getJumlah().toString(), null, Element.ALIGN_CENTER));
					table.addCell(
							buatIsiTableCell(Converter.patternCurrency(d.getSubTotal()), null, Element.ALIGN_CENTER));
					table.addCell(buatIsiTableCell(Converter.patternCurrency(d.getHargaJual()), null, Element.ALIGN_CENTER));
				}

				table.setSpacingAfter(1f);
				document.add(table);

				PdfPTable tabelRincianBayar = new PdfPTable(4);
				tabelRincianBayar.setWidthPercentage(100);
				tabelRincianBayar.setWidths(new int[] { 1, 1, 1, 1 });

				tabelRincianBayar.addCell(buatIsiTableCell("Total Pembelian", headFont, Element.ALIGN_CENTER));
				tabelRincianBayar.addCell(buatIsiTableCell("Diskon", headFont, Element.ALIGN_CENTER));
				tabelRincianBayar.addCell(buatIsiTableCell("Pajak", headFont, Element.ALIGN_CENTER));
				tabelRincianBayar.addCell(buatIsiTableCell("Total Akhir", headFont, Element.ALIGN_CENTER));

				BigDecimal grandTotal = p.getGrandTotal();
				BigDecimal grandPajak = p.getPajak();
				BigDecimal grandDiskon = p.getDiskon();

				tabelRincianBayar.addCell(
						buatIsiTableCell(Converter.patternCurrency(p.getTotal()), null, Element.ALIGN_CENTER));
				tabelRincianBayar
						.addCell(buatIsiTableCell(Converter.patternCurrency(grandDiskon), null, Element.ALIGN_CENTER));
				tabelRincianBayar
						.addCell(buatIsiTableCell(Converter.patternCurrency(grandPajak), null, Element.ALIGN_CENTER));
				tabelRincianBayar
						.addCell(buatIsiTableCell(Converter.patternCurrency(grandTotal), null, Element.ALIGN_CENTER));
				
				sumGrandDiskon = sumGrandDiskon.add(grandDiskon);
				sumGrandPajak = sumGrandPajak.add(grandPajak);
				sumGrandTotal = sumGrandTotal.add(grandTotal);

				tabelRincianBayar.setSpacingAfter(23f);
				document.add(tabelRincianBayar);
			}

			PdfPTable tabelSum = new PdfPTable(3);
			tabelSum.setWidthPercentage(100);
			tabelSum.setWidths(new int[] { 1, 1, 1 });
			tabelSum.addCell(buatIsiTableCell("TOTAL PAJAK", headFont, Element.ALIGN_CENTER));
			tabelSum.addCell(buatIsiTableCell("TOTAL DISKON", headFont, Element.ALIGN_CENTER));
			tabelSum.addCell(buatIsiTableCell("TOTAL PEMBELIAN", headFont, Element.ALIGN_CENTER));
			tabelSum.addCell(buatIsiTableCell(Converter.patternCurrency(sumGrandPajak), null, Element.ALIGN_CENTER));
			tabelSum.addCell(buatIsiTableCell(Converter.patternCurrency(sumGrandDiskon), null, Element.ALIGN_CENTER));
			tabelSum.addCell(buatIsiTableCell(Converter.patternCurrency(sumGrandTotal), null, Element.ALIGN_CENTER));
			tabelSum.setSpacingAfter(3f);
			document.add(tabelSum);
			document.close();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return new ByteArrayInputStream(out.toByteArray());
	}

	private PdfPCell buatHeaderTableCell(String isi, Font font) {
		PdfPCell cell;
		if (font == null) {
			cell = new PdfPCell(new Phrase(isi));
		} else {
			cell = new PdfPCell(new Phrase(isi, font));
		}
		cell.setHorizontalAlignment(Element.ALIGN_CENTER);
		cell.setPadding(5f);
		return cell;
	}

	private PdfPCell buatIsiTableCell(String isi, Font font, int align) {
		PdfPCell cell;
		if (font == null) {
			cell = new PdfPCell(new Phrase(isi));
		} else {
			cell = new PdfPCell(new Phrase(isi, font));
		}
		cell.setHorizontalAlignment(align);
		cell.setPadding(5f);
		return cell;
	}

	private PdfPTable subheader(String a, String b) throws DocumentException {
		PdfPTable table = new PdfPTable(2);
		table.setSpacingAfter(5f);
		table.setWidthPercentage(100);
		table.setWidths(new int[] { 1, 1 });
		table.getDefaultCell().setBorder(Rectangle.NO_BORDER);

		Phrase p1 = new Phrase("Tanggal: " + a, new Font(FontFamily.HELVETICA, 16));
		PdfPCell kiri = new PdfPCell(p1);
		kiri.setHorizontalAlignment(Element.ALIGN_LEFT);
		kiri.setBorder(Rectangle.NO_BORDER);
		table.addCell(kiri);

		if (!StringUtils.equals(b, "KOSONG")) {
			Phrase p2 = new Phrase("Tipe: " + b, new Font(FontFamily.HELVETICA, 16));
			PdfPCell kanan = new PdfPCell(p2);
			kanan.setHorizontalAlignment(Element.ALIGN_RIGHT);
			kanan.setBorder(Rectangle.NO_BORDER);
			table.addCell(kanan);
		}

		return table;
	}
	
	private PdfPTable subheader(String a) throws DocumentException {
		PdfPTable table = new PdfPTable(2);
		table.setSpacingAfter(5f);
		table.setWidthPercentage(100);
		table.setWidths(new int[] { 1, 1 });
		table.getDefaultCell().setBorder(Rectangle.NO_BORDER);

		Phrase p1 = new Phrase("Tanggal: " + a, new Font(FontFamily.HELVETICA, 16));
		PdfPCell kiri = new PdfPCell(p1);
		kiri.setHorizontalAlignment(Element.ALIGN_LEFT);
		kiri.setBorder(Rectangle.NO_BORDER);
		table.addCell(kiri);


		return table;
	}

	private PdfPTable kolomPenjualanDetail(String a, String b, String c) throws DocumentException {
		PdfPTable table = new PdfPTable(3);
		// table.setSpacingAfter(3f);
		table.setWidthPercentage(100);
		table.setHorizontalAlignment(Element.ALIGN_LEFT);
		table.setWidths(new int[] { 1, 1, 1 });
		table.getDefaultCell().setBorder(Rectangle.NO_BORDER);

		Font subHeaderFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD);

		Phrase p1 = new Phrase(a, subHeaderFont);
		Phrase p2 = new Phrase(b, subHeaderFont);
		Phrase p3 = new Phrase(c, subHeaderFont);
		table.addCell(p1);
		table.addCell(p2);
		table.addCell(p3);
		return table;
	}

}
