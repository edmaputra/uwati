<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>

<%@ include file="../../layouts/taglib.jsp"%>

<c:url var="dapatkanPenjualan" value="/laporan/pembatalan/dapatkan/jual" />
<c:url var="dapatkanPembelian" value="/laporan/pembatalan/dapatkan/beli" />
<c:url var="daftar" value="/laporan/pembatalan/daftar" />

<c:url var="pdfUrl" value="/laporan/penjualan/pdf" />
<c:url var="excel" value="/laporan/penjualan/excel" />

<div class="row">
	<div class="col-lg-12">
		<div class="content-panel">
			<div class="row kolom-pencarian">
				<div class="col-md-12">
					<form class="form-inline pull-right" id="formCari">
						<div class="form-group">
							<select class="form-control" name="tipe" id="tipe"
								style="width: 165px;">
								<option value="0">Penjualan</option>
								<option value="1">Pembelian</option>
								<option value="2">Retur Pembelian</option>
							</select>
						</div>

						<div class="form-group">
							<input type="text" id="tanggalAwal"
								class="form-control datePicker" placeholder="Tanggal Awal"
								style="width: 110px" />
						</div>
						<div class="form-group">
							<input type="text" id="tanggalAkhir"
								class="form-control datePicker" placeholder="Tanggal Akhir"
								style="width: 110px" />
						</div>
						<div class="form-group">
							<input type="text" id="cari" class="form-control"
								placeholder="Pencarian" style="width: 150px" />
						</div>
						<div class="form-group">
							<a class="btn btn-primary" id="btnCari" style="width: 80px;">Filter</a>
						</div>
						<div class="form-group">
							<a class="btn btn-default" id="btnReset">Reset</a>							
						</div>
					</form>
				</div>
			</div>
		</div>

		<div class="content-panel">

			<table class="table table-striped table-advance table-hover"
				id="tabelCoba">
			</table>
			<div id="nav"></div>
		</div>
	</div>
</div>

<div class="modal container fade" id="penjualan-modal" tabindex="-1"
	style="display: none;">
	<div class="modal-header">
		<button type="button" class="close" data-dismiss="modal"
			aria-hidden="true">&times;</button>
		<h4 class="modal-title" id="myModalLabel">Laporan</h4>
	</div>
	<form class="form style-form" method="post">
		<div class="modal-body">
			<div class="row">
				<div class="form-group col-md-3">
					<label class="lb-sm">Nomor Faktur</label> <input type="text"
						readonly="readonly" class="form-control" id="penjualan_nomor_faktur" />
				</div>
				<div class="form-group col-md-2">
					<label class="lb-sm">Tanggal Transaksi</label> <input type="text"
						readonly="readonly" class="form-control" id="penjualan_tanggal_transaksi" />
				</div>
				<div class="form-group col-md-2">
					<label class="lb-sm">Tanggal Pembatalan</label> <input type="text"
						readonly="readonly" class="form-control" id="penjualan_tanggal_pembatalan" />
				</div>				
				<div class="form-group col-md-5">
					<label class="lb-sm">Alasan Pembatalan</label> <input type="text"
						readonly="readonly" class="form-control" id="penjualan_alasan_pembatalan" />
				</div>
			</div>
			<div class="row">
				<div class="form-group col-md-4">
					<label class="lb-sm">Kasir</label> <input type="text"
						readonly="readonly" class="form-control" id="penjualan_kasir" />
				</div>
				<div class="form-group col-md-4">
					<label class="lb-sm">Pasien/Pelanggan</label> <input type="text"
						readonly="readonly" class="form-control" id="penjualan_pelanggan" />
				</div>
				<div class="form-group col-md-4">
					<label class="lb-sm">Dokter</label> <input type="text"
						readonly="readonly" class="form-control" id="penjualan_dokter" />
				</div>
			</div>
			<div class="row">
				<div class="form-group col-md-2">
					<label class="lb-sm">Total (Bruto)</label> <input type="text"
						readonly="readonly" class="form-control input-angka"
						id="penjualan_total_bruto" />
				</div>
				<div class="form-group col-md-2">
					<label class="lb-sm">Diskon</label> <input type="text"
						readonly="readonly" class="form-control input-angka" id="penjualan_diskon" />
				</div>
				<div class="form-group col-md-2">
					<label class="lb-sm">Pajak</label> <input type="text"
						readonly="readonly" class="form-control input-angka" id="penjualan_pajak" />
				</div>
				<div class="form-group col-md-2">
					<label class="lb-sm">Total (Netto)</label> <input type="text"
						readonly="readonly" class="form-control input-angka"
						id="penjualan_total_netto" />
				</div>
				<div class="form-group col-md-2">
					<label class="lb-sm">Pembayaran</label> <input type="text"
						readonly="readonly" class="form-control input-angka" id="penjualan_bayar" />
				</div>
				<div class="form-group col-md-2">
					<label class="lb-sm">Kembali</label> <input type="text"
						readonly="readonly" class="form-control input-angka" id="penjualan_kembali" />
				</div>
			</div>
			<div class="row">
				<div class="col-md-12">
					<table class="table table-striped table-advance table-hover"
						id="penjualan_tabel_detail" border="1"></table>
				</div>
			</div>
		</div>
		<div class="modal-footer">
			<button type="button" class="btn btn-default btnKeluar"
				data-dismiss="modal">Keluar</button>
			<input type="hidden" name="id" class="form-control" id="ids" />
		</div>
	</form>
</div>

<div class="modal container fade" id="pembelian-modal" tabindex="-1"
	style="display: none;">
	<div class="modal-header">
		<button type="button" class="close" data-dismiss="modal"
			aria-hidden="true">&times;</button>
		<h4 class="modal-title" id="myModalLabel">Laporan</h4>
	</div>
	<form class="form style-form" method="post">
		<div class="modal-body">
			<div class="row">
				<div class="form-group col-md-3">
					<label class="lb-sm">Nomor Faktur</label> <input type="text"
						readonly="readonly" class="form-control" id="pembelian_nomor_faktur" />
				</div>
				<div class="form-group col-md-2">
					<label class="lb-sm">Tanggal Transaksi</label> <input type="text"
						readonly="readonly" class="form-control" id="pembelian_tanggal_transaksi" />
				</div>
				<div class="form-group col-md-4">
					<label class="lb-sm">Supplier/Distributor</label> <input
						type="text" readonly="readonly" class="form-control" id="pembelian_supplier" />
				</div>
			</div>
			<div class="row">
			<div class="form-group col-md-2">
					<label class="lb-sm">Tanggal Pembatalan</label> <input type="text"
						readonly="readonly" class="form-control" id="pembelian_tanggal_pembatalan" />
				</div>
				<div class="form-group col-md-5">
					<label class="lb-sm">Alasan Pembatalan</label> <input type="text"
						readonly="readonly" class="form-control" id="pembelian_alasan_pembatalan" />
				</div>
			</div>
			<div class="row">
				<div class="form-group col-md-3">
					<label class="lb-sm">Total Pembelian (Bruto)</label> <input type="text"
						readonly="readonly" class="form-control input-angka"
						id="pembelian_total_bruto" />
				</div>
				<div class="form-group col-md-3">
					<label class="lb-sm">Diskon</label> <input type="text"
						readonly="readonly" class="form-control input-angka" id="pembelian_diskon" />
				</div>
				<div class="form-group col-md-3">
					<label class="lb-sm">Pajak</label> <input type="text"
						readonly="readonly" class="form-control input-angka" id="pembelian_pajak" />
				</div>
				<div class="form-group col-md-3">
					<label class="lb-sm">Total Pembelian (Netto)</label> <input type="text"
						readonly="readonly" class="form-control input-angka"
						id="pembelian_total_netto" />
				</div>
			</div>
			<div class="row">
				<div class="col-md-12">
					<table class="table table-striped table-advance table-hover"
						id="pembelian_tabel_detail" border="1"></table>
				</div>
			</div>
		</div>
		<div class="modal-footer"></div>
	</form>
</div>

<div>
	<%@ include file="../../layouts/gritter.jsp"%>
</div>


<script>
	var state = 1;
	var tipe = 0;
	var tanggalAwal = tanggalHariIni();
	var tanggalAkhir = '';
	var cari = '';
	$(document).ready(function() {
		$('#tanggalAwal').val(tanggalAwal);
		refresh(1, tipe, tanggalAwal, tanggalAkhir, cari);

		$('#btnCari').click(function() {
			tipe = $('#tipe').val();
			tanggalAwal = $('#tanggalAwal').val();
			tanggalAkhir = $('#tanggalAkhir').val();
			cari = $('#cari').val();
			refresh(1, tipe, tanggalAwal, tanggalAkhir, cari);
		});

		$('#btnReset').click(function() {
			reset();
			refresh(1, tipe, tanggalAwal, tanggalAkhir, cari);
		});

		$('#button_rekap').click(function() {
			getDataRekap();
		});
		
		$(".form-batal").validate({
			rules : {
				info_batal : {
					required : true
				}
			},
			messages : {
				info_batal : {
					required : "Harap Isi Alasan Pembatalan"
				}
			},
			submitHandler : function(form) {
				var data = {};
				data['id'] = $('#hapusId').val();
				data['info'] = $('#info_batal').val();				
				resetBatal();
				$.postJSON('${batal}', data, function(result) {					
					$('#keluarModalHapus').click();
					$('#gritter-hapus-sukses').click();
					reset();
					refresh(1, tipe, tanggalAwal, tanggalAkhir, cari);
				}, function(e) {
					$('#keluarModalHapus').click();
					$('#gritter-hapus-sukses').click();
					reset();
					refresh(1, tipe, tanggalAwal, tanggalAkhir, cari);
				});
			}
		});
	});

	function getDataBatalPembelian(ids) {
		var data = {
			id : ids
		};
		$.getAjax('${dapatkanPembelian}', data, function(result) {
			isiDataPembelian(result);
		}, null);
	}
	
	function getDataBatalPenjualan(ids) {
		var data = {
			id : ids
		};
		$.getAjax('${dapatkanPenjualan}', data, function(result) {
			isiDataPenjualan(result);
		}, null);
	}

	function refresh(halaman, tipe, tanggalAwal, tanggalAkhir, cari) {
		var data = {
			hal : halaman,
			tipe : tipe,
			tanggalAwal : tanggalAwal,
			tanggalAkhir : tanggalAkhir,
			cari : cari
		};
		$.getAjax('${daftar}', data, function(result) {
			$('#tabelCoba').empty();
			$('#tabelCoba').append(result.tabel);
			$('#nav').empty();
			$('#nav').append(result.navigasiHalaman);
			$('#rekapitulasi').empty();
			$('#rekapitulasi').append(result.grandTotal);
		}, null);
	}

	function isiDataPenjualan(result) {		
		$('#penjualan_nomor_faktur').val(result.nomorFaktur);
		$('#penjualan_tanggal_transaksi').val(dateFormat(result.waktuTransaksi, 'dd-mm-yyyy'));
		$('#penjualan_tanggal_pembatalan').val(dateFormat(result.waktuPembatalan, 'dd-mm-yyyy'));
		$('#penjualan_alasan_pembatalan').val(result.info);
		$('#penjualan_pelanggan').val(result.pelanggan);
		$('#penjualan_kasir').val(result.pengguna);
		$('#penjualan_dokter').val(result.dokter);
		
		$('#penjualan_total_bruto').val(result.totalPembelian);
		$('#penjualan_diskon').val(result.diskon);
		$('#penjualan_pajak').val(result.pajak);
		$('#penjualan_total_netto').val(result.grandTotal);
		$('#penjualan_bayar').val(result.bayar);
		$('#penjualan_kembali').val(result.kembali);
		$('#penjualan_tabel_detail').empty();
		$('#penjualan_tabel_detail').append(result.details);
	}
	
	function isiDataPembelian(result) {		
		$('#pembelian_nomor_faktur').val(result.nomorFaktur);
		$('#pembelian_tanggal_transaksi').val(dateFormat(result.waktuTransaksi, 'dd-mm-yyyy'));
		$('#pembelian_tanggal_pembatalan').val(dateFormat(result.waktuPembatalan, 'dd-mm-yyyy'));
		$('#pembelian_alasan_pembatalan').val(result.info);
		$('#pembelian_supplier').val(result.distributor);		
		
		
		$('#pembelian_total_bruto').val(result.totalPembelian);
		$('#pembelian_diskon').val(result.diskon);
		$('#pembelian_pajak').val(result.pajak);
		$('#pembelian_total_netto').val(result.totalPembelianFinal);		
		$('#pembelian_tabel_detail').empty();
		$('#pembelian_tabel_detail').append(result.details);
	}

	function reset() {
		tipe = 0;
		tanggalAwal = tanggalHariIni();
		tanggalAkhir = '';
		cari = '';
		$('#tipe').val(tipe);
		$('#tanggalAwal').val(tanggalAwal);
		$('#tanggalAkhir').val(tanggalAkhir);
		$('#cari').val(cari);
	}
	
	function resetBatal(){
		$('#info_batal').val('');
	}
</script>