package pzpi_22_9_tymofiienko_snizhana_design_pattern

import "fmt"

// Інтерфейс реалізації
type Device interface {
	IsEnabled() bool
	Enable()
	Disable()
	GetVolume() int
	SetVolume(int)
}

// Абстракція
type Remote struct {
	device Device
}

func NewRemote(d Device) *Remote {
	return &Remote{device: d}
}

func (r *Remote) TogglePower() {
	if r.device.IsEnabled() {
		r.device.Disable()
	} else {
		r.device.Enable()
	}
}

func (r *Remote) VolumeUp() {
	r.device.SetVolume(r.device.GetVolume() + 10)
}

// Розширена абстракція
type AdvancedRemote struct {
	*Remote
}

func NewAdvancedRemote(d Device) *AdvancedRemote {
	return &AdvancedRemote{NewRemote(d)}
}

func (r *AdvancedRemote) Mute() {
	r.device.SetVolume(0)
}

// Конкретна реалізація: Телевізор
type Tv struct {
	enabled bool
	volume  int
}

func (t *Tv) IsEnabled() bool { return t.enabled }
func (t *Tv) Enable()         { t.enabled = true; fmt.Println("TV: enabled") }
func (t *Tv) Disable()        { t.enabled = false; fmt.Println("TV: disabled") }
func (t *Tv) GetVolume() int  { return t.volume }
func (t *Tv) SetVolume(v int) { t.volume = v; fmt.Println("TV volume set to", v) }

// Конкретна реалізація: Радіо
type Radio struct {
	enabled bool
	volume  int
}

func (r *Radio) IsEnabled() bool { return r.enabled }
func (r *Radio) Enable()         { r.enabled = true; fmt.Println("Radio: enabled") }
func (r *Radio) Disable()        { r.enabled = false; fmt.Println("Radio: disabled") }
func (r *Radio) GetVolume() int  { return r.volume }
func (r *Radio) SetVolume(v int) { r.volume = v; fmt.Println("Radio volume set to", v) }

// Клієнтський код
func main() {
	tv := &Tv{}
	remote := NewRemote(tv)
	remote.TogglePower()
	remote.VolumeUp()

	radio := &Radio{}
	advRemote := NewAdvancedRemote(radio)
	advRemote.TogglePower()
	advRemote.Mute()
}
