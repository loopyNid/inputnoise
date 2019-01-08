Server.default.waitForBoot({
/*
	goal:
	reload the sample every time the python script sends a completion message.
	Then feed the sample to a GrainBuf synthDef.

	steps:
	1. make synth def
*/
// SynthDef(\grain, {|amp = 0.5, trigFreq = 100, grainDur = 0.1,  filePos = 0.5, rate = 1, pan = 0|
// 	var src;
// 	src = GrainBuf.ar(1, Impulse.kr(trigFreq), SinOsc.kr(0.01)*grainDur, b.bufnum, rate, SinOsc.kr(0.1, mul: 0.43, add: filePos), pan, c.bufnum)!2;
// 	Out.ar(0, src * amp);
// }).add;
Server.default.waitForBoot({
	(
		~counter= true;
		b = Buffer.read(s, "/var/log/sc/noise.wav", bufnum: 1);
		d = Buffer.read(s, "/var/log/sc/noise.wav", bufnum: 2);
		c = Buffer.sendCollection(s, Env.sine().discretize);
		SynthDef(\grain, {|amp = 0.3, trigFreq = 100, grainDur = 0.1,  filePos = 0.5, rate = 1, pan = 0, phasorRate = 1, gate = 1 , bufnum = 1|
			var src, env, envgen;
			env = Env.asr(attackTime: 10, releaseTime: 15);
			envgen = EnvGen.kr(env, gate: gate, doneAction: 2);
			src = GrainBuf.ar(1, Impulse.kr(trigFreq), SinOsc.kr(0.01)*grainDur, bufnum, rate, Phasor.kr(rate: phasorRate), pan, c.bufnum)!2;
			Out.ar(0, src * amp * envgen);
			"grain ADDED".postln;
		}).add;
		SynthDef(\grain2, {|amp = 0.3, trigFreq = 100, grainDur = 0.1,  filePos = 0.5, rate = 1, pan = 0, phasorRate = 1, gate = 1 , bufnum = 2|
			var src, env, envgen;
			env = Env.asr(attackTime: 10, releaseTime: 10);
			envgen = EnvGen.kr(env, gate: gate, doneAction: 2);
			src = GrainBuf.ar(1, Impulse.kr(trigFreq), SinOsc.kr(0.01)*grainDur, bufnum, rate, Phasor.kr(rate: phasorRate), pan, c.bufnum)!2;
			Out.ar(0, src * amp * envgen);
			"grain ADDED".postln;
		}).add;

		OSCdef.new(\reciever,{| msg, time, addr, port|
			if(~counter,{
				j.set(\gate, 0);
				d.free;
				d = Buffer.read(s, "/var/log/sc/noise.wav", bufnum: 2);
				~counter.postln;
				~counter=false;
				k = Synth(\grain2,[\grainDur, 1.0.linrand,\rate, 1.0.rand, \phasorRate, 1.0.rand, \bufnum, 2]);
			},
				{
					k.set(\gate, 0);
					b.free;
					b = Buffer.read(s, "/var/log/sc/noise.wav", bufnum: 1);
					~counter.postln;
					~counter=true;
					j = Synth(\grain,[\grainDur, 1.0.linrand,\rate, 1.0.rand, \phasorRate, 1.0.rand, \bufnum, 1]);
				}
			);
			// b = Buffer.read(s, "/var/log/sc/noise.wav", bufnum: 1);
			// j = Synth(\grain,[\grainDur, 1.0.linrand,\rate, 1.0.rand, \phasorRate, 1.0.rand]);
			msg.postln;
		},'/reloadSample', recvPort: 6666);
	)
});
});
