package id.edmaputra.uwati.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import id.edmaputra.uwati.entity.master.Apotek;
import id.edmaputra.uwati.entity.master.Karyawan;
import id.edmaputra.uwati.entity.master.Kategori;
import id.edmaputra.uwati.entity.master.Pelanggan;
import id.edmaputra.uwati.entity.master.Satuan;
import id.edmaputra.uwati.entity.master.obat.Obat;
import id.edmaputra.uwati.entity.master.obat.ObatDetail;
import id.edmaputra.uwati.entity.master.obat.ObatExpired;
import id.edmaputra.uwati.entity.master.obat.ObatStok;
import id.edmaputra.uwati.entity.master.obat.Racikan;
import id.edmaputra.uwati.entity.master.obat.RacikanDetail;
import id.edmaputra.uwati.entity.pengguna.Pengguna;
import id.edmaputra.uwati.entity.pengguna.Role;
import id.edmaputra.uwati.repository.ApotekRepository;
import id.edmaputra.uwati.repository.KaryawanRepository;
import id.edmaputra.uwati.repository.KategoriRepository;
import id.edmaputra.uwati.repository.PelangganRepository;
import id.edmaputra.uwati.repository.SatuanRepository;
import id.edmaputra.uwati.repository.obat.ObatDetailRepository;
import id.edmaputra.uwati.repository.obat.ObatExpiredRepository;
import id.edmaputra.uwati.repository.obat.ObatRepository;
import id.edmaputra.uwati.repository.obat.ObatStokRepository;
import id.edmaputra.uwati.repository.obat.RacikanDetailRepository;
import id.edmaputra.uwati.repository.obat.RacikanRepository;
import id.edmaputra.uwati.repository.pengguna.PenggunaRepository;
import id.edmaputra.uwati.repository.pengguna.RoleRepository;

@Service
public class Init {

	@Autowired
	private PenggunaRepository penggunaRepo;

	@Autowired
	private RoleRepository roleRepo;

	@Autowired
	private SatuanRepository satuanRepo;

	@Autowired
	private ApotekRepository apotekRepo;

	@Autowired
	private KategoriRepository kategoriRepo;

	@Autowired
	private KaryawanRepository dokterRepo;

	@Autowired
	private PelangganRepository pelangganRepo;

	@Autowired
	private ObatRepository obatRepo;

	@Autowired
	private ObatDetailRepository obatDetailRepo;

	@Autowired
	private ObatStokRepository obatStokRepo;

	@Autowired
	private ObatExpiredRepository obatExpiredRepo;

	@Autowired
	private RacikanRepository racikanRepository;

	@Autowired
	private RacikanDetailRepository racikanDetailRepository;

//	@PostConstruct
	public void mulai() {
		Role roleAdmin = buatRole("ROLE_ADMIN");
		roleRepo.save(roleAdmin);

		Role roleApotik = buatRole("ROLE_APOTIK");
		roleRepo.save(roleApotik);

		Role roleMedis = buatRole("ROLE_MEDIS");
		roleRepo.save(roleMedis);

		List<Role> roles = new ArrayList<>();
		roles.add(roleAdmin);

		List<Role> rolesApotik = new ArrayList<>();
		rolesApotik.add(roleApotik);

		List<Role> rolesMediss = new ArrayList<>();
		rolesMediss.add(roleMedis);
		rolesMediss.add(roleApotik);

		Pengguna pengguna = buatPengguna("admin", true, true, roles);
//		Pengguna apotik = buatPengguna("apotik", true, true, rolesApotik);
//		Pengguna medis = buatPengguna("medis", true, true, rolesMediss);
//
		penggunaRepo.save(pengguna);
//		penggunaRepo.save(apotik);
//		penggunaRepo.save(medis);
		
		Karyawan k = new Karyawan();
		k = buatDokter("Bangun", "MATA", "1111", "Kota Bangun", "Dokter",null);
		k.setPengguna(pengguna);
		dokterRepo.save(k);
		
		pengguna.setKaryawan(k);
		penggunaRepo.save(pengguna);
//
		Satuan pcs = buatSatuan("PCS");
		satuanRepo.save(pcs);

		Satuan botol = buatSatuan("BOTOL");
		satuanRepo.save(botol);

		Satuan kapsul = buatSatuan("KAPSUL");
		satuanRepo.save(kapsul);

		Satuan kaplet = buatSatuan("KAPLET");
		satuanRepo.save(kaplet);
//
//		for (int i = 0; i < 50; i++) {
//			Satuan sat = buatSatuan("SATUAN" + i);
//			satuanRepo.save(sat);
//		}
//
		Kategori ok = buatKategori("OBAT KERAS");
		kategoriRepo.save(ok);

		Kategori ll = buatKategori("LAIN-LAIN");
		kategoriRepo.save(ll);
//
//		for (int i = 0; i < 7; i++) {
//			Dokter d = buatDokter("DOKTER " + i, "SPESIALIS " + i, i + "XXXX", "Jln. " + i);
//			dokterRepo.save(d);
//		}
//
//		for (int i = 0; i < 10; i++) {
//			Pelanggan p = buatPelanggan("Pelanggan " + i, "K" + i, "Komplek" + i, "0811" + i);
//			pelangganRepo.save(p);
//		}
//		//
		for (int i = 0; i < 70; i++) {
			Obat obat = buatObat("OBAT " + i, "K-" + i, kaplet, ll, null, null, null, null, 50, 0);
			obatRepo.save(obat);

			ObatDetail obatDetail = buatObatDetail(obat, new BigDecimal(i * 10000), new BigDecimal(i * 9000),
					new BigDecimal(i * 1000), new BigDecimal(i * 100));
			obatDetailRepo.save(obatDetail);

			ObatStok obatStok = buatObatStok(obat, new Integer(i * 100));
			obatStokRepo.save(obatStok);

			ObatExpired obatExpired = buatObatExpired(obat, new Date());
			obatExpiredRepo.save(obatExpired);
		}
		//
		Apotek a = buatApotek("APOTEK FERRY", "Jalan Jelarai Tanjung Selor", "0552-12121");
		a.setBiayaResep(BigDecimal.ZERO);
		apotekRepo.save(a);

		// int i = 1;
		// for (Obat o : obatRepo.findAll()){
		// Racikan r = buatRacikan("ABC"+ i, i*1100, i*1000);
		// racikanRepository.save(r);
		//
		// RacikanDetail rd = buatRacikanDetail(r, o, i, i*100);
		// racikanDetailRepository.save(rd);
		//
		//
		//
		// i++;
		// }
	}

	private Role buatRole(String nama) {
		Role role = new Role();
		role.setUserInput("admin");
		role.setWaktuDibuat(new Date());
		role.setTerakhirDirubah(new Date());
		role.setNama(nama);
		return role;
	}

	private Pengguna buatPengguna(String nama, boolean isAktif, boolean isPertamaKali, List<Role> roles) {
		Pengguna pengguna = new Pengguna(nama, null, true, true, 5, roles, null);
		pengguna.setUserInput("admin");
		pengguna.setWaktuDibuat(new Date());
		pengguna.setTerakhirDirubah(new Date());
		pengguna.setRoles(roles);
		BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		String password = passwordEncoder.encode("admin");
		pengguna.setKataSandi(password);
		return pengguna;
	}

	private Satuan buatSatuan(String nama) {
		Satuan satuan = new Satuan(nama);
		satuan.setUserInput("admin");
		satuan.setWaktuDibuat(new Date());
		satuan.setTerakhirDirubah(new Date());
		return satuan;
	}

	private Apotek buatApotek(String nama, String alamat, String telepon) {
		Apotek apotek = new Apotek(nama, alamat, telepon);
		apotek.setUserInput("admin");
		apotek.setWaktuDibuat(new Date());
		apotek.setTerakhirDirubah(new Date());
		return apotek;
	}

	private Kategori buatKategori(String nama) {
		Kategori kategori = new Kategori(nama);
		kategori.setUserInput("admin");
		kategori.setWaktuDibuat(new Date());
		kategori.setTerakhirDirubah(new Date());
		return kategori;
	}

	private Karyawan buatDokter(String nama, String spesialis, String sip, String alamat, String jabatan, Pengguna pengguna) {
		Karyawan karyawan = new Karyawan(nama, spesialis, sip, alamat, jabatan, pengguna);
		karyawan.setUserInput("admin");
		karyawan.setWaktuDibuat(new Date());
		karyawan.setTerakhirDirubah(new Date());
		return karyawan;
	}

	private Pelanggan buatPelanggan(String nama, String kode, String alamat, String kontak) {
		Pelanggan pelanggan = new Pelanggan(nama, kode, alamat, kontak);
		pelanggan.setUserInput("admin");
		pelanggan.setWaktuDibuat(new Date());
		pelanggan.setTerakhirDirubah(new Date());
		return pelanggan;
	}

	private ObatExpired buatObatExpired(Obat obat, Date tanggalExpired) {
		ObatExpired obatExpired = new ObatExpired(obat, tanggalExpired);
		obatExpired.setUserInput("admin");
		obatExpired.setWaktuDibuat(new Date());
		obatExpired.setTerakhirDirubah(new Date());
		return obatExpired;
	}

	private ObatDetail buatObatDetail(Obat obat, BigDecimal hargaJual, BigDecimal hargaBeli, BigDecimal hargaDiskon,
			BigDecimal hargaJualResep) {
		ObatDetail obatDetail = new ObatDetail(obat, hargaJual, hargaBeli, hargaDiskon, hargaJualResep);
		obatDetail.setUserInput("admin");
		obatDetail.setWaktuDibuat(new Date());
		obatDetail.setTerakhirDirubah(new Date());
		return obatDetail;
	}

	private ObatStok buatObatStok(Obat obat, Integer stok) {
		ObatStok obatStok = new ObatStok(obat, stok);
		obatStok.setUserInput("admin");
		obatStok.setWaktuDibuat(new Date());
		obatStok.setTerakhirDirubah(new Date());
		return obatStok;
	}

	private Obat buatObat(String nama, String kode, Satuan satuan, Kategori kategori, String batch, String barcode,
			List<ObatDetail> obatDetail, List<ObatStok> stok, Integer stokMinimal, Integer tipe) {
		Obat obat = new Obat(nama, kode, satuan, kategori, batch, barcode, obatDetail, stok, stokMinimal, tipe);
		obat.setUserInput("admin");
		obat.setWaktuDibuat(new Date());
		obat.setTerakhirDirubah(new Date());
		return obat;
	}

	private Racikan buatRacikan(String nama, int hargaJual, int biayaRacik) {
		Racikan racikan = new Racikan(nama, new BigDecimal(hargaJual), new BigDecimal(biayaRacik));
		racikan.setUserInput("admin");
		racikan.setWaktuDibuat(new Date());
		racikan.setTerakhirDirubah(new Date());
		return racikan;
	}

	private RacikanDetail buatRacikanDetail(Racikan racikan, Obat obat, int jumlah, int hargaSatuan) {
		RacikanDetail racikanDetail = new RacikanDetail(racikan, obat, jumlah, new BigDecimal(hargaSatuan));
		racikanDetail.setUserInput("admin");
		racikanDetail.setWaktuDibuat(new Date());
		racikanDetail.setTerakhirDirubah(new Date());
		return racikanDetail;
	}
}
