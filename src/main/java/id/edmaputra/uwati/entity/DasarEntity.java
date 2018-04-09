package id.edmaputra.uwati.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

import id.edmaputra.uwati.DBConf;
import id.edmaputra.uwati.view.Table;
import lombok.Data;

@MappedSuperclass
@Data
public abstract class DasarEntity<T> implements Serializable {

	private static final long serialVersionUID = 1L;

	@Column(name = "creator", nullable = true, length = DBConf.LENGTH_NAMA_PENGGUNA) 
	private String userInput;

	@Column(name = "editor", nullable = true, length = DBConf.LENGTH_NAMA_PENGGUNA)
	private String userEditor;

	@NotNull(message = "Tanggal Dibuat Null")
	@Column(name = "waktu_dibuat", nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date waktuDibuat;

	@NotNull(message = "Tanggal Terakhir Dirubah Null")
	@Column(name = "terakhir_dirubah", nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date terakhirDirubah;
	
	@Column(name = "info", nullable = true, length = DBConf.LENGTH_NAMA)
	private String info;
}
