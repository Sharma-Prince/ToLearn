# Android Lifecycle — Detailed Cheat Sheet (Activity & Fragment)

This file expands the previous cheat sheet by listing **common real-world cases** and **exact lifecycle callback sequences** — with special attention to **when `onPause()` is called** and what happens next. Use this as a single-reference `.md` to understand behavior across scenarios.

---

## Quick summary — when is `onPause()` called?

`onPause()` is invoked when an Activity (or Fragment's UI) **loses foreground focus** — that is, it is no longer the interactive activity. Typical triggers:
- Another activity (or system dialog) comes in front (partially or fully).  
- The user presses Home or switches apps.  
- A transparent/overlay activity covers part of the screen (this may call `onPause()` but not `onStop()`).  
- A runtime permission / incoming call / lock screen / other system UI appears.  
- The Activity is about to be destroyed (e.g., due to `finish()` or configuration change) — `onPause()` is called before `onStop()`/`onDestroy()`.

**Important:** Some system kills (low memory) can stop your process without `onDestroy()` being called. Always save important UI state in `onSaveInstanceState()` and rely on `ViewModel` for longer-lived state.

---

# Activity lifecycle baseline (normal start / stop)

```
Launch: onCreate() -> onStart() -> onResume()  (Activity running and interactive)
Leave:  onPause() -> onStop() -> (maybe onDestroy())
```

---

## Cases for Activity — sequences and when `onPause()` happens

For each case below, the **sequence** shows the callbacks invoked on the existing (previous) activity and any relevant notes. When a new activity appears, the previous activity typically receives the listed callbacks.

### 1) Normal cold start (app launched)
**Sequence (launched activity):**  
`onCreate()` → `onStart()` → `onResume()`  
**`onPause()`?** No (activity gains focus).

### 2) Launch a full-screen activity on top (e.g., open new activity)
**Sequence (previous activity):**  
`onPause()` → `onStop()` (if completely covered)  
**When `onPause()`?** Immediately when the new activity takes focus; `onStop()` after the previous activity is no longer visible.

### 3) Launch a translucent/ dialog-themed activity (partial overlay)
**Sequence (previous activity):**  
`onPause()` (but usually **no** `onStop()` while overlay visible)  
**When `onPause()`?** When overlay takes focus. Because the underlying activity remains visible, it often does not get `onStop()`.

### 4) Show an in-app dialog (`AlertDialog` on same Activity)
**Sequence:**  
_no activity lifecycle callbacks_ (dialog is part of same window)  
**When `onPause()`?** Not called — since the activity remains focused and owns the dialog. Use dialog callbacks instead.

### 5) Home button / switch to another app
**Sequence (previous activity):**  
`onPause()` → `onStop()`  
**When `onPause()`?** Immediately as your activity loses focus; `onStop()` after it is no longer visible.

### 6) Recent apps / Overview screen
**Sequence:**  
`onPause()` → `onStop()`  
**When `onPause()`?** Same as Home — activity loses focus and may be stopped.

### 7) Back pressed and activity finishes
**Sequence:**  
`onPause()` → `onStop()` → `onDestroy()`  
**When `onPause()`?** Called before finishing; then full teardown follows.

### 8) Incoming call or full-screen system UI (phone call, alarm)
**Sequence (depends on whether system UI is full-screen):**  
- If full-screen: `onPause()` → `onStop()`  
- If overlay/translucent: `onPause()` (no `onStop()`)  
**When `onPause()`?** When the call UI takes focus.

### 9) Runtime permission dialog (system permission UI)
**Sequence:**  
`onPause()` (permission dialog is a new activity on top) → later `onResume()` when permission granted/denied  
**When `onPause()`?** Permission dialog takes focus, so `onPause()` is invoked.

### 10) Configuration change (rotation, locale change) — default (activity recreated)
**Sequence (old instance):**  
`onPause()` → `onSaveInstanceState()` → `onStop()` → `onDestroy()`  
**Sequence (new instance):**  
`onCreate(savedInstanceState)` → `onStart()` → `onRestoreInstanceState()` → `onResume()`  
**When `onPause()`?** Always called before teardown. Use `onSaveInstanceState()` to persist UI state.

> Note: If you handle configuration changes (`android:configChanges`) yourself, the activity may **not** be destroyed — but this is discouraged for many scenarios.

### 11) Multi-window / split-screen
**Behavior:** The activity can be visible but not focused. When the activity **loses focus** (another pane gets focus): `onPause()` is called. When it becomes not visible at all, it may receive `onStop()`.  
**When `onPause()`?** When your window loses focus.

### 12) Picture-in-Picture (PIP)
**Behavior:** When entering PIP, Android will call lifecycle events and `onPictureInPictureModeChanged()` is delivered. Exact callback order depends on Android version and your implementation. Often the activity will move away from full interaction (may receive `onPause()`), but you should use `onUserLeaveHint()`/PIP callbacks to prepare.  
**When `onPause()`?** Possibly, if activity loses focus; don't rely solely on `onPause()` — also handle `onPictureInPictureModeChanged()`.

### 13) System kills process (low memory)
**Behavior:** The system **may kill** the process without calling `onDestroy()` (or even `onStop()` in extreme cases). `onSaveInstanceState()` is not guaranteed in every path; but the system attempts to call `onSaveInstanceState()` before killing when appropriate.  
**When `onPause()`?** May have been called earlier; but **do not** assume `onDestroy()` will run. Persist important data proactively.

### 14) Starting a Service / Background work (no UI change)
**Behavior:** Starting a service does not change activity lifecycle by itself. `onPause()` is not invoked when you start a background service unless a new UI element appears.

---

# Fragment lifecycle baseline

```
Attach:   onAttach() -> onCreate() -> onCreateView() -> onViewCreated() -> onStart() -> onResume()
Tear-down: onPause() -> onStop() -> onDestroyView() -> onDestroy() -> onDetach()
```

> Fragments are tied to their host Activity lifecycle, but they have additional view-related callbacks (`onCreateView()` / `onDestroyView()`). When the activity is paused, fragments get `onPause()` as well.

---

## Cases for Fragment — sequences and `onPause()` behavior

### 1) Add fragment shown (normal)
**Sequence:**  
`onAttach()` → `onCreate()` → `onCreateView()` → `onViewCreated()` → `onStart()` → `onResume()`  
**`onPause()`?** Not called until fragment loses foreground (host activity pauses or fragment is replaced).

### 2) Replace fragment **with** `addToBackStack()` (old fragment kept in backstack)
**Sequence (old fragment — being replaced):**  
`onPause()` → `onStop()` → `onDestroyView()`  
- **Important:** The fragment instance remains (not `onDestroy()`/`onDetach()`), so its non-view state remains in memory and it can be restored when popped.  
**When `onPause()`?** When the new fragment takes focus (or the host activity remains resumed but the fragment's view is removed).

**Sequence (when you pop back to it):**  
`onCreateView()` → `onViewCreated()` → `onStart()` → `onResume()`

### 3) Replace fragment **without** backstack (old fragment removed)
**Sequence (old fragment):**  
`onPause()` → `onStop()` → `onDestroyView()` → `onDestroy()` → `onDetach()`  
**When `onPause()`?** When it loses focus / is removed.

### 4) Remove fragment (programmatically)
**Sequence:** Same as replace without backstack: `onPause()` → `onStop()` → `onDestroyView()` → `onDestroy()` → `onDetach()`

### 5) Hide / Show (`FragmentTransaction.hide()` / `show()`)
**Behavior:** `hide()` simply changes fragment visibility; **does not** call `onPause()` / `onStop()` / `onDestroyView()` by itself — the fragment stays STARTED/RESUMED if host Activity is resumed. Use `onHiddenChanged()` or lifecycle observations to respond.  
**When `onPause()`?** Not called just for hide/show.

### 6) ViewPager / ViewPager2 (offscreen fragments)
**Behavior:** ViewPager keeps adjacent fragments in certain lifecycle states depending on offscreen limit. Typically only the primary page is `RESUMED`; others may be `STARTED`. When a fragment becomes primary it receives `onResume()`; when it ceases to be primary it receives `onPause()`.  
**When `onPause()`?** When the fragment loses primary visibility (user swiped away) or when host Activity pauses.

### 7) DialogFragment
**Behavior:** DialogFragment controls a dialog window. While the dialog is shown, the fragment lifecycle is similar (attached/resumed). If a new full-screen activity appears, `onPause()` will be delivered.  
**When `onPause()`?** When the dialog loses focus or host activity pauses.

### 8) Retained fragments / configuration change
**Behavior:** If you used `setRetainInstance(true)` (deprecated) or ViewModel, the fragment instance can survive configuration change; however the fragment's **view is destroyed** and recreated: `onDestroyView()` will run, but `onDestroy()` may not.  
**Sequence (on rotation):** `onPause()` → `onSaveInstanceState()` → `onStop()` → `onDestroyView()` → (fragment instance retained) → later `onCreateView()` -> `onViewCreated()` -> `onStart()` -> `onResume()`  
**When `onPause()`?** Yes, before the view is torn down.

### 9) Host Activity paused/stopped/destroyed
**Behavior:** Fragments mirror host lifecycle. If Activity receives `onPause()` → fragment `onPause()` will be called; Activity `onStop()` → fragment `onStop()`, etc. If Activity is destroyed, fragments go through destroy sequence as well.  
**When `onPause()`?** When host Activity loses focus.

### 10) Fragment transaction using `commitNow()` vs `commit()`
**Behavior:** `commitNow()` applies changes immediately (callbacks happen synchronously). `commit()` schedules them for the next frame. Both will lead to same lifecycle transitions but timing differs.  
**When `onPause()`?** Same triggers: when fragment's UI is removed or stopped.

---

# Compact table: common triggers → will `onPause()` run?

- Start app / become foreground → **No**.  
- Open full-screen activity in front → **Yes**.  
- Open translucent/dialog activity → **Yes** (but **no** `onStop()` if still visible).  
- Home / overview → **Yes**.  
- Back (finish) → **Yes**.  
- Permission / system dialog → **Yes**.  
- In-app dialog → **No**.  
- Rotate (config change) → **Yes**.  
- System kill (low memory) → **Maybe** (system may kill without final callbacks).  
- Fragment replaced (backstack) → **Yes** for `onPause()` → `onDestroyView()` (fragment instance kept).  
- Fragment hidden via `hide()` → **No**.

---

# Practical tips / best practices

- Put lightweight UI-save logic in `onPause()` (e.g., commit typed text to ViewModel/DB). Heavy work belongs in background threads or `onStop()`. Keep `onPause()` fast.  
- Use `onSaveInstanceState()` to persist UI state needed to recreate the Activity/Fragment process.  
- Release view bindings and resources referencing the view in `onDestroyView()` for Fragments (avoid leaks).  
- Use `ViewModel` for data that must survive configuration changes.  
- Don’t rely on `onDestroy()` for critical persistence — the system may not call it before killing your process.

---

## Appendix: Example sequences (quick reference)

**A. Activity A → start Activity B (full screen):**  
A: `onPause()` → `onStop()`  
B: `onCreate()` → `onStart()` → `onResume()`

**B. Activity A → start Activity B (translucent):**  
A: `onPause()`  
B: `onCreate()` → `onStart()` → `onResume()`

**C. Fragment F1 replaced by F2 (added to backstack):**  
F1: `onPause()` → `onStop()` → `onDestroyView()`  
F2: `onAttach()` → `onCreate()` → `onCreateView()` → `onViewCreated()` → `onStart()` → `onResume()`

**D. Press Home from Activity A:**  
A: `onPause()` → `onStop()` (system may later call `onDestroy()` if needed)

---

If you'd like, I can also:

- generate **PNG/SVG flow diagrams** for each case so the sequences render anywhere, or
- convert this into a compact **cheat-sheet card** (one-page PDF), or
- add **Mermaid** code blocks for each scenario for quick visual editing.
