package id.edmaputra.uwati.controller.laporan;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

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

import id.edmaputra.uwati.entity.transaksi.Penjualan;
import id.edmaputra.uwati.entity.transaksi.PenjualanDetail;
import id.edmaputra.uwati.support.Converter;

public class PdfGenerator {

	public ByteArrayInputStream laporanPenjualan(List<Penjualan> penjualans, String tanggalAwal, String tanggalAkhir, String tipe) {
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

			document.add(subheader(tanggalAwal+" s/d "+tanggalAkhir, tipe));

			for (Penjualan p : penjualans) {
				document.add(kolomPenjualanDetail("Tanggal Transaksi: " + Converter.dateToString(p.getWaktuTransaksi()),
						"Nomor Faktur: " + p.getNomorFaktur(), ""));
				PdfPTable detail = kolomPenjualanDetail("Dokter: " + p.getDokter(), "Pasien: " + p.getPelanggan(),
						"Kasir: " + p.getPengguna());
				detail.setSpacingAfter(3f);
				document.add(detail);
				
				PdfPTable table = new PdfPTable(4);
				table.setWidthPercentage(100);
				table.setWidths(new int[] { 3, 1, 1, 1 });
			
				table.addCell(buatHeaderTableCell("Obat", headFont));
				table.addCell(buatHeaderTableCell("Harga Jual", headFont));
				table.addCell(buatHeaderTableCell("Jumlah", headFont));
				table.addCell(buatHeaderTableCell("Total", headFont));
								
				for(PenjualanDetail d : p.getPenjualanDetail()) {
					 table.addCell(buatIsiTableCell(d.getObat(), null, Element.ALIGN_LEFT));
					 table.addCell(buatIsiTableCell(Converter.patternCurrency(d.getHargaJual()), null, Element.ALIGN_CENTER));
					 table.addCell(buatIsiTableCell(d.getJumlah().toString(), null, Element.ALIGN_CENTER));
					 table.addCell(buatIsiTableCell(Converter.patternCurrency(d.getHargaTotal()), null, Element.ALIGN_CENTER));					
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
				
				tabelRincianBayar.addCell(buatIsiTableCell(Converter.patternCurrency(p.getTotalPembelian()), null, Element.ALIGN_CENTER));
				tabelRincianBayar.addCell(buatIsiTableCell(Converter.patternCurrency(p.getDiskon()), null, Element.ALIGN_CENTER));
				tabelRincianBayar.addCell(buatIsiTableCell(Converter.patternCurrency(p.getPajak()), null, Element.ALIGN_CENTER));
				tabelRincianBayar.addCell(buatIsiTableCell(Converter.patternCurrency(p.getGrandTotal()), null, Element.ALIGN_CENTER));
				tabelRincianBayar.addCell(buatIsiTableCell(Converter.patternCurrency(p.getBayar()), null, Element.ALIGN_CENTER));
				tabelRincianBayar.addCell(buatIsiTableCell(Converter.patternCurrency(p.getKembali()), null, Element.ALIGN_CENTER));
				
				tabelRincianBayar.setSpacingAfter(23f);
				document.add(tabelRincianBayar);	
			}
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
		Phrase p2 = new Phrase("Tipe: " + b, new Font(FontFamily.HELVETICA, 16));
		
		PdfPCell kiri = new PdfPCell(p1);
		kiri.setHorizontalAlignment(Element.ALIGN_LEFT);
		kiri.setBorder(Rectangle.NO_BORDER);
		
		PdfPCell kanan = new PdfPCell(p2);
		kanan.setHorizontalAlignment(Element.ALIGN_RIGHT);
		kanan.setBorder(Rectangle.NO_BORDER);
		
		table.addCell(kiri);
		table.addCell(kanan);
		return table;
	}

	private PdfPTable kolomPenjualanDetail(String a, String b, String c) throws DocumentException {
		PdfPTable table = new PdfPTable(3);
//		table.setSpacingAfter(3f);
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
