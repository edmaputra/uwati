package id.edmaputra.uwati.specification;

import java.util.Date;

import com.mysema.query.types.expr.BooleanExpression;

import id.edmaputra.uwati.entity.transaksi.QBatalPembelian;
import id.edmaputra.uwati.entity.transaksi.QBatalPenjualan;
import id.edmaputra.uwati.entity.transaksi.QBatalReturPembelianDetail;

public class BatalPenjualanPredicateBuilder {

	private BooleanExpression hasil = null;

	public BooleanExpression getExpression() {
		return hasil;
	}

	public void cari(String cari) {
		if (hasil == null) {
			hasil = QBatalPenjualan.batalPenjualan.nomorFaktur.containsIgnoreCase(cari)
					.or(QBatalPenjualan.batalPenjualan.dokter.containsIgnoreCase(cari))
					.or(QBatalPenjualan.batalPenjualan.pelanggan.containsIgnoreCase(cari))
					.or(QBatalPenjualan.batalPenjualan.nomorResep.containsIgnoreCase(cari));
		} else {
			hasil = hasil.and(QBatalPenjualan.batalPenjualan.nomorFaktur.containsIgnoreCase(cari)
					.or(QBatalPenjualan.batalPenjualan.dokter.containsIgnoreCase(cari))
					.or(QBatalPenjualan.batalPenjualan.pelanggan.containsIgnoreCase(cari))
					.or(QBatalPenjualan.batalPenjualan.nomorResep.containsIgnoreCase(cari)));
		}
	}

	public void tanggal(Date from, Date to) {
		if (hasil == null) {
			hasil = QBatalPenjualan.batalPenjualan.waktuPembatalan.between(from, to);
		} else {
			hasil = hasil.and(QBatalPenjualan.batalPenjualan.waktuPembatalan.between(from, to));
		}
	}

}
