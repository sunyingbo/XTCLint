package com.xtc.lint;
public class DbNECMusic {
    private Integer id;
    private String songId;
    private String name;
    private String type;
    private String albumName;
    private Long albumId;
    private Long albumArtistId;
    private String albumArtistName;
    private String coverUrl;
    private String coverLocalPath;
    private Long mvId;
    private Long duration;
    private Boolean canPlay;
    private Long downloadId;
    private Integer downloadStatus;
    private String localPath;
    private Boolean isFavorite;
    private String completeTime;
    private Integer fileSize;

    public DbNECMusic() {
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getSongId() {
        return this.songId;
    }

    public void setSongId(String songId) {
        this.songId = songId;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAlbumName() {
        return this.albumName;
    }

    public void setAlbumName(String albumName) {
        this.albumName = albumName;
    }

    public Long getAlbumId() {
        return this.albumId;
    }

    public void setAlbumId(Long albumId) {
        this.albumId = albumId;
    }

    public Long getAlbumArtistId() {
        return this.albumArtistId;
    }

    public void setAlbumArtistId(Long albumArtistId) {
        this.albumArtistId = albumArtistId;
    }

    public String getAlbumArtistName() {
        return this.albumArtistName;
    }

    public void setAlbumArtistName(String albumArtistName) {
        this.albumArtistName = albumArtistName;
    }

    public String getCoverUrl() {
        return this.coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public String getCoverLocalPath() {
        return this.coverLocalPath;
    }

    public void setCoverLocalPath(String coverLocalPath) {
        this.coverLocalPath = coverLocalPath;
    }

    public Long getMvId() {
        return this.mvId;
    }

    public void setMvId(Long mvId) {
        this.mvId = mvId;
    }

    public Long getDuration() {
        return this.duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    public Boolean getCanPlay() {
        return this.canPlay;
    }

    public void setCanPlay(Boolean canPlay) {
        this.canPlay = canPlay;
    }

    public Long getDownloadId() {
        return this.downloadId;
    }

    public void setDownloadId(Long downloadId) {
        this.downloadId = downloadId;
    }

    public Integer getDownloadStatus() {
        return this.downloadStatus;
    }

    public void setDownloadStatus(Integer downloadStatus) {
        this.downloadStatus = downloadStatus;
    }

    public String getLocalPath() {
        return this.localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    public Boolean getFavorite() {
        return this.isFavorite;
    }

    public void setFavorite(Boolean favorite) {
        this.isFavorite = favorite;
    }

    public String getCompleteTime() {
        return this.completeTime;
    }

    public void setCompleteTime(String completeTime) {
        this.completeTime = completeTime;
    }

    public Integer getFileSize() {
        return this.fileSize;
    }

    public void setFileSize(Integer fileSize) {
        this.fileSize = fileSize;
    }

    public String toString() {
        return "DbNECMusic{id=" + this.id + ", songId='" + this.songId + '\'' + ", name='" + this.name + '\'' + ", type='" + this.type + '\'' + ", albumName='" + this.albumName + '\'' + ", albumId=" + this.albumId + ", albumArtistId=" + this.albumArtistId + ", albumArtistName='" + this.albumArtistName + '\'' + ", coverUrl='" + this.coverUrl + '\'' + ", coverLocalPath='" + this.coverLocalPath + '\'' + ", mvId=" + this.mvId + ", duration=" + this.duration + ", canPlay=" + this.canPlay + ", downloadId='" + this.downloadId + '\'' + ", downloadStatus=" + this.downloadStatus + ", localPath='" + this.localPath + '\'' + ", isFavorite=" + this.isFavorite + ", completeTime='" + this.completeTime + '\'' + ", fileSize=" + this.fileSize + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DbNECMusic that = (DbNECMusic) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (songId != null ? !songId.equals(that.songId) : that.songId != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (type != null ? !type.equals(that.type) : that.type != null) return false;
        if (albumName != null ? !albumName.equals(that.albumName) : that.albumName != null)
            return false;
        if (albumId != null ? !albumId.equals(that.albumId) : that.albumId != null) return false;
        if (albumArtistId != null ? !albumArtistId.equals(that.albumArtistId) : that.albumArtistId != null)
            return false;
        if (albumArtistName != null ? !albumArtistName.equals(that.albumArtistName) : that.albumArtistName != null)
            return false;
        if (coverUrl != null ? !coverUrl.equals(that.coverUrl) : that.coverUrl != null)
            return false;
        if (coverLocalPath != null ? !coverLocalPath.equals(that.coverLocalPath) : that.coverLocalPath != null)
            return false;
        if (mvId != null ? !mvId.equals(that.mvId) : that.mvId != null) return false;
        if (duration != null ? !duration.equals(that.duration) : that.duration != null)
            return false;
        if (canPlay != null ? !canPlay.equals(that.canPlay) : that.canPlay != null) return false;
        if (downloadId != null ? !downloadId.equals(that.downloadId) : that.downloadId != null)
            return false;
        if (downloadStatus != null ? !downloadStatus.equals(that.downloadStatus) : that.downloadStatus != null)
            return false;
        if (localPath != null ? !localPath.equals(that.localPath) : that.localPath != null)
            return false;
        if (isFavorite != null ? !isFavorite.equals(that.isFavorite) : that.isFavorite != null)
            return false;
        if (completeTime != null ? !completeTime.equals(that.completeTime) : that.completeTime != null)
            return false;
        return fileSize != null ? fileSize.equals(that.fileSize) : that.fileSize == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (songId != null ? songId.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (albumName != null ? albumName.hashCode() : 0);
        result = 31 * result + (albumId != null ? albumId.hashCode() : 0);
        result = 31 * result + (albumArtistId != null ? albumArtistId.hashCode() : 0);
        result = 31 * result + (albumArtistName != null ? albumArtistName.hashCode() : 0);
        result = 31 * result + (coverUrl != null ? coverUrl.hashCode() : 0);
        result = 31 * result + (coverLocalPath != null ? coverLocalPath.hashCode() : 0);
        result = 31 * result + (mvId != null ? mvId.hashCode() : 0);
        result = 31 * result + (duration != null ? duration.hashCode() : 0);
        result = 31 * result + (canPlay != null ? canPlay.hashCode() : 0);
        result = 31 * result + (downloadId != null ? downloadId.hashCode() : 0);
        result = 31 * result + (downloadStatus != null ? downloadStatus.hashCode() : 0);
        result = 31 * result + (localPath != null ? localPath.hashCode() : 0);
        result = 31 * result + (isFavorite != null ? isFavorite.hashCode() : 0);
        result = 31 * result + (completeTime != null ? completeTime.hashCode() : 0);
        result = 31 * result + (fileSize != null ? fileSize.hashCode() : 0);
        return result;
    }
}
