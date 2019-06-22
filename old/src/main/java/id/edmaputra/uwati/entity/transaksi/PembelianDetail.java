package id.edmaputra.uwati.entity.transaksi;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import id.edmaputra.uwati.entity.DasarTransaksiEntity;
import lombok.Data;

@Entity
@Table(name = "pembelian_detail", uniqueConstraints = { @UniqueConstraint(columnNames = "id")})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@Data
public class PembelianDetail extends DasarTransaksiEntity<Long> {

	private static final long serialVersionUID = -8422941488327899895L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotNull(message = "Obat Null")
	@Column(nullable = false, name = "obat")
	private String obat;
	
//	@NotNull(message = "Obat Null")
	@Column(nullable = false, name = "id_obat")
	private Long idObat;

	@NotNull(message = "Jumlah Null")
	@Column(nullable = false)	
	private Integer jumlah;
		
	@NotNull(message = "Harga Jual Null")
	private BigDecimal hargaJual;
	
	@NotNull(message = "Harga Jual Resep Null")
	private BigDecimal hargaJualResep;
	
	@NotNull(message = "Harga Beli Null")
	private BigDecimal hargaBeli;

	@NotNull(message = "Diskon Null")
	@Column(columnDefinition="Decimal(19,2) default 0")
	private BigDecimal diskon;

	@NotNull(message = "Pajak Null")
	private BigDecimal pajak;

	@NotNull(message = "Subtotal Null")
	private BigDecimal subTotal;

	@NotNull(message = "Tanggal Kadaluarsa Null")
	private Date tanggalKadaluarsa;

	@NotNull(message = "Pembelian Null")
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_pembelian")
	@JsonIgnore
	private Pembelian pembelian;

	@NotNull(message = "Status Retur Null")
	private Boolean isReturned;
}
