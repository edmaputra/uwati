package id.edmaputra.uwati.controller.laporan;

import java.security.Principal;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.mysema.query.types.expr.BooleanExpression;

import id.edmaputra.uwati.entity.transaksi.BatalPembelian;
import id.edmaputra.uwati.entity.transaksi.BatalPembelianDetail;
import id.edmaputra.uwati.entity.transaksi.BatalPenjualan;
import id.edmaputra.uwati.entity.transaksi.BatalPenjualanDetail;
import id.edmaputra.uwati.entity.transaksi.BatalReturPembelianDetail;
import id.edmaputra.uwati.entity.transaksi.PembelianDetail;
import id.edmaputra.uwati.service.transaksi.BatalPembelianDetailService;
import id.edmaputra.uwati.service.transaksi.BatalPembelianService;
import id.edmaputra.uwati.service.transaksi.BatalPenjualanDetailService;
import id.edmaputra.uwati.service.transaksi.BatalPenjualanService;
import id.edmaputra.uwati.service.transaksi.BatalReturPembelianDetailService;
import id.edmaputra.uwati.specification.BatalPembelianPredicateBuilder;
import id.edmaputra.uwati.specification.BatalPenjualanPredicateBuilder;
import id.edmaputra.uwati.specification.BatalReturPembelianDetailPredicateBuilder;
import id.edmaputra.uwati.support.Converter;
import id.edmaputra.uwati.support.LogSupport;
import id.edmaputra.uwati.view.Formatter;
import id.edmaputra.uwati.view.Html;
import id.edmaputra.uwati.view.HtmlElement;
import id.edmaputra.uwati.view.THead;
import id.edmaputra.uwati.view.Table;
import id.edmaputra.uwati.view.handler.PembelianDetailHandler;
import id.edmaputra.uwati.view.handler.PenjualanHandler;

@Controller
@RequestMapping("/laporan/pembatalan")
public class LaporanPembatalanController {

	private static final Logger logger = LoggerFactory.getLogger(LaporanPembatalanController.class);

	@Autowired
	private BatalPenjualanService batalPenjualanService;

	@Autowired
	private BatalPenjualanDetailService batalPenjualanDetailService;

	@Autowired
	private BatalPembelianService batalPembelianService;

	@Autowired
	private BatalPembelianDetailService batalPembelianDetailService;

	@Autowired
	private BatalReturPembelianDetailService batalReturService;

	@RequestMapping(method = RequestMethod.GET)
	public ModelAndView tampilkanPelanggan(Principal principal, HttpServletRequest request) {
		try {
			logger.info(LogSupport.load(principal.getName(), request));
			ModelAndView mav = new ModelAndView("laporan-pembatalan");
			return mav;
		} catch (Exception e) {
			logger.info(e.getMessage());
			return null;
		}
	}

	@RequestMapping(value = "/daftar", method = RequestMethod.GET)
	@ResponseBody
	public HtmlElement daftar(@RequestParam(value = "hal", defaultValue = "1", required = false) Integer halaman,
			@RequestParam(value = "tipe", defaultValue = "1", required = false) Integer tipe,
			@RequestParam(value = "tanggalAwal", defaultValue = "", required = false) String tanggalAwal,
			@RequestParam(value = "tanggalAkhir", defaultValue = "", required = false) String tanggalAkhir,
			@RequestParam(value = "cari", defaultValue = "", required = false) String cari, HttpServletRequest request,
			HttpServletResponse response) {
		try {
			HtmlElement el = new HtmlElement();

			if (tipe == 0) {
				BatalPenjualanPredicateBuilder builder = new BatalPenjualanPredicateBuilder();

				if (StringUtils.isNotBlank(tanggalAwal) || StringUtils.isNotBlank(tanggalAkhir)) {
					Date awal = Converter.stringToDate(tanggalAwal);
					if (StringUtils.isBlank(tanggalAkhir)) {
						builder.tanggal(awal, awal);
					} else if (StringUtils.isNotBlank(tanggalAkhir)) {
						Date akhir = Converter.stringToDate(tanggalAkhir);
						if (awal.compareTo(akhir) > 0) {
							builder.tanggal(akhir, awal);
						} else if (awal.compareTo(akhir) < 0) {
							builder.tanggal(awal, akhir);
						}
					}
				}

				if (!StringUtils.isBlank(cari)) {
					builder.cari(cari);
				}

				BooleanExpression exp = builder.getExpression();
				Page<BatalPenjualan> batalPenjualans = batalPenjualanService.muatDaftar(halaman, exp);

				String tabel = tabelBatalPenjualan(batalPenjualans, request);
				el.setTabel(tabel);

				if (batalPenjualans.hasContent()) {
					int current = batalPenjualans.getNumber() + 1;
					int next = current + 1;
					int prev = current - 1;
					int first = Math.max(1, current - 5);
					int last = Math.min(first + 10, batalPenjualans.getTotalPages());

					String h = Html.navigasiHalamanGenerator(first, prev, current, next, last,
							batalPenjualans.getTotalPages(), tipe, tanggalAwal, tanggalAkhir, cari);
					el.setNavigasiHalaman(h);
				}
			} else if (tipe == 1) {
				BatalPembelianPredicateBuilder builder = new BatalPembelianPredicateBuilder();

				if (StringUtils.isNotBlank(tanggalAwal) || StringUtils.isNotBlank(tanggalAkhir)) {
					Date awal = Converter.stringToDate(tanggalAwal);
					if (StringUtils.isBlank(tanggalAkhir)) {
						builder.tanggal(awal, awal);
					} else if (StringUtils.isNotBlank(tanggalAkhir)) {
						Date akhir = Converter.stringToDate(tanggalAkhir);
						if (awal.compareTo(akhir) > 0) {
							builder.tanggal(akhir, awal);
						} else if (awal.compareTo(akhir) < 0) {
							builder.tanggal(awal, akhir);
						}
					}
				}

				if (!StringUtils.isBlank(cari)) {
					builder.cari(cari);
				}

				BooleanExpression exp = builder.getExpression();
				Page<BatalPembelian> batalPembelians = batalPembelianService.muatDaftar(halaman, exp);

				String tabel = tabelBatalPembelian(batalPembelians, request);
				el.setTabel(tabel);

				if (batalPembelians.hasContent()) {
					int current = batalPembelians.getNumber() + 1;
					int next = current + 1;
					int prev = current - 1;
					int first = Math.max(1, current - 5);
					int last = Math.min(first + 10, batalPembelians.getTotalPages());

					String h = Html.navigasiHalamanGenerator(first, prev, current, next, last,
							batalPembelians.getTotalPages(), tipe, tanggalAwal, tanggalAkhir, cari);
					el.setNavigasiHalaman(h);
				}
			} else if (tipe == 2) {
				BatalReturPembelianDetailPredicateBuilder builder = new BatalReturPembelianDetailPredicateBuilder();

				if (StringUtils.isNotBlank(tanggalAwal) || StringUtils.isNotBlank(tanggalAkhir)) {
					Date awal = Converter.stringToDate(tanggalAwal);
					if (StringUtils.isBlank(tanggalAkhir)) {
						builder.tanggal(awal, awal);
					} else if (StringUtils.isNotBlank(tanggalAkhir)) {
						Date akhir = Converter.stringToDate(tanggalAkhir);
						if (awal.compareTo(akhir) > 0) {
							builder.tanggal(akhir, awal);
						} else if (awal.compareTo(akhir) < 0) {
							builder.tanggal(awal, akhir);
						}
					}
				}

				if (!StringUtils.isBlank(cari)) {
					builder.cari(cari);
				}

				BooleanExpression exp = builder.getExpression();
				Page<BatalReturPembelianDetail> batalPenjualans = batalReturService.muatDaftar(halaman, exp);

				String tabel = tabelBatalReturPembelian(batalPenjualans, request);
				el.setTabel(tabel);

				if (batalPenjualans.hasContent()) {
					int current = batalPenjualans.getNumber() + 1;
					int next = current + 1;
					int prev = current - 1;
					int first = Math.max(1, current - 5);
					int last = Math.min(first + 10, batalPenjualans.getTotalPages());

					String h = Html.navigasiHalamanGenerator(first, prev, current, next, last,
							batalPenjualans.getTotalPages(), tipe, tanggalAwal, tanggalAkhir, cari);
					el.setNavigasiHalaman(h);
				}
			}

			return el;
		} catch (Exception e) {
			logger.info(e.getMessage());
			return null;
		}
	}

	private String tabelBatalPenjualan(Page<BatalPenjualan> list, HttpServletRequest request) {
		String html = "";
		String data = "";
		for (BatalPenjualan p : list.getContent()) {
			String row = "";
			String btn = "";
			row += Html.td(Converter.dateToString(p.getWaktuPembatalan()));
			row += Html.td(Table.nullCell(p.getInfo()));
			row += Html.td(Converter.dateToString(p.getWaktuTransaksi()));
			row += Html.td(Table.nullCell(p.getNomorFaktur()));
			row += Html.td(Table.nullCell(p.getPelanggan()));
			row += Html.td(Table.nullCell(p.getPengguna()));
			row += Html.td(Table.nullCell(p.getDokter()));
			row += Html.td(Formatter.patternCurrency(p.getGrandTotal()));
			btn += Html.button("btn btn-primary btn-xs btnEdit", "modal", "#penjualan-modal", "onClick",
					"getDataBatalPenjualan(" + p.getId() + ")", 0, "Detail Penjualan " + p.getNomorFaktur());

			row += Html.td(btn);
			data += Html.tr(row);
		}
		String tbody = Html.tbody(data);
		html = THead.THEAD_BATAL_PENJUALAN + tbody;
		return html;
	}

	private String tabelBatalPembelian(Page<BatalPembelian> list, HttpServletRequest request) {
		String html = "";
		String data = "";
		for (BatalPembelian p : list.getContent()) {
			String row = "";
			String btn = "";
			row += Html.td(Converter.dateToString(p.getWaktuPembatalan()));
			row += Html.td(Table.nullCell(p.getInfo()));
			row += Html.td(Converter.dateToString(p.getWaktuTransaksi()));
			row += Html.td(Table.nullCell(p.getNomorFaktur()));
			row += Html.td(Table.nullCell(p.getSupplier()));
			row += Html.td(Table.nullCell(p.getPengguna()));
			row += Html.td(Formatter.patternCurrency(p.getGrandTotal()));
			btn += Html.button("btn btn-primary btn-xs btnEdit", "modal", "#pembelian-modal", "onClick",
					"getDataBatalPembelian(" + p.getId() + ")", 0, "Detail Pembelian " + p.getNomorFaktur());

			row += Html.td(btn);
			data += Html.tr(row);
		}
		String tbody = Html.tbody(data);
		html = THead.THEAD_BATAL_PEMBELIAN + tbody;
		return html;
	}

	private String tabelBatalReturPembelian(Page<BatalReturPembelianDetail> list, HttpServletRequest request) {
		String html = "";
		String data = "";
		for (BatalReturPembelianDetail p : list.getContent()) {
			String row = "";
			String btn = "";
			row += Html.td(Converter.dateToString(p.getWaktuPembatalan()));
			row += Html.td(Table.nullCell(p.getInfo()));
			row += Html.td(Converter.dateToString(p.getWaktuTransaksi()));
			row += Html.td(Table.nullCell(p.getNomorFaktur()));
			row += Html.td(Table.nullCell(p.getSupplier()));
			row += Html.td(Table.nullCell(p.getObat()));
			row += Html.td(Formatter.patternCurrency(p.getHargaBeli()));
			row += Html.td(Formatter.patternCurrency(p.getJumlah()));
			row += Html.td(Formatter.patternCurrency(p.getHargaTotal()));
			// btn += Html.button("btn btn-primary btn-xs btnEdit", "modal",
			// "#pembelian-modal", "onClick",
			// "getData(" + p.getId() + ", 1)", 0, "Detail Retur Pembelian " +
			// p.getNomorFaktur());

			row += Html.td(btn);
			data += Html.tr(row);
		}
		String tbody = Html.tbody(data);
		html = THead.THEAD_BATAL_RETUR_PEMBELIAN + tbody;
		return html;
	}

	@RequestMapping(value = "/dapatkan/jual", method = RequestMethod.GET)
	@ResponseBody
	public PenjualanHandler dapatkan(@RequestParam("id") String id) {
		try {
			PenjualanHandler ph = new PenjualanHandler();
			BatalPenjualan get = batalPenjualanService.dapatkan(new Long(id));
			List<BatalPenjualanDetail> details = batalPenjualanDetailService.dapatkanByBatalPenjualan(get);

			ph = setContent(get, ph);
			//
			ph.setDetails(tabelDetails(details));

			return ph;
		} catch (Exception e) {
			logger.info(e.getMessage());
			return null;
		}
	}

	@RequestMapping(value = "/dapatkan/beli", method = RequestMethod.GET)
	@ResponseBody
	public PembelianDetailHandler dapatkanBeli(@RequestParam("id") String id) {
		try {
			PembelianDetailHandler ph = new PembelianDetailHandler();
			BatalPembelian get = batalPembelianService.dapatkan(new Long(id));
			List<BatalPembelianDetail> details = batalPembelianDetailService.dapatkanByBatalPembelian(get);
			ph = setBatalPembelianContent(get, ph);
			ph.setDetails(tabelBatalPembelianDetails(details));
			return ph;
		} catch (Exception e) {
			logger.info(e.getMessage());
			return null;
		}
	}

	private PembelianDetailHandler setBatalPembelianContent(BatalPembelian p, PembelianDetailHandler ph) {
		ph.setNomorFaktur(p.getNomorFaktur());
		ph.setDistributor(p.getSupplier());
		ph.setTanggal(Converter.dateToStringDashSeparator(p.getWaktuTransaksi()));
		ph.setTotalPembelian(Formatter.patternCurrency(p.getTotal()));
		ph.setDiskon(Formatter.patternCurrency(p.getDiskon()));
		ph.setPajak(Formatter.patternCurrency(p.getPajak()));
		ph.setTotalPembelianFinal(Formatter.patternCurrency(p.getGrandTotal()));
		ph.setInfo(p.getInfo());
		ph.setWaktuPembatalan(p.getWaktuPembatalan());
		return ph;
	}

	private String tabelBatalPembelianDetails(List<BatalPembelianDetail> pembelianDetails) {
		String html = "";
		String data = "";
		for (BatalPembelianDetail d : pembelianDetails) {
			String row = "";
			row += Html.td(d.getObat());
			row += Html.td(Formatter.patternCurrency(d.getHargaBeli()));
			row += Html.td(Formatter.patternCurrency(d.getJumlah()));
			row += Html.td(Formatter.patternCurrency(d.getSubTotal()));
			row += Html.td(Formatter.patternCurrency(d.getHargaJual()));
			data += Html.tr(row);
		}
		String tbody = Html.tbody(data);
		html = THead.THEAD_PEMBELIAN_DETAIL + tbody;
		return html;
	}

	private PenjualanHandler setContent(BatalPenjualan p, PenjualanHandler ph) {
		ph.setNomorFaktur(p.getNomorFaktur());
		ph.setPengguna(p.getPengguna());
		ph.setDokter(p.getDokter());
		ph.setPelanggan(p.getPelanggan());
		ph.setWaktuTransaksi(p.getWaktuTransaksi());
		ph.setTotalPembelian(Formatter.patternCurrency(p.getTotalPembelian()));
		ph.setDiskon(Formatter.patternCurrency(p.getDiskon()));
		ph.setPajak(Formatter.patternCurrency(p.getPajak()));
		ph.setGrandTotal(Formatter.patternCurrency(p.getGrandTotal()));
		ph.setBayar(Formatter.patternCurrency(p.getBayar()));
		ph.setKembali(Formatter.patternCurrency(p.getKembali()));
		ph.setInfo(p.getInfo());
		ph.setWaktuPembatalan(p.getWaktuPembatalan());
		return ph;
	}

	private String tabelDetails(List<BatalPenjualanDetail> penjualanDetails) {
		String html = "";
		String data = "";
		for (BatalPenjualanDetail d : penjualanDetails) {
			String row = "";
			row += Html.td(d.getObat());
			row += Html.td(Formatter.patternCurrency(d.getHargaJual()));
			row += Html.td(Formatter.patternCurrency(d.getJumlah()));
			row += Html.td(Formatter.patternCurrency(d.getHargaTotal()));
			data += Html.tr(row);
		}
		String tbody = Html.tbody(data);

		html = THead.THEAD_PENJUALAN_DETAIL + tbody;

		return html;
	}
}
